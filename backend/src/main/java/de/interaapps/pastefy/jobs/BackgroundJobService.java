package de.interaapps.pastefy.jobs;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.BackgroundJob;
import org.javawebstack.orm.Repo;
import org.javawebstack.orm.query.Query;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackgroundJobService {
    private static final Logger LOGGER = Logger.getLogger(BackgroundJobService.class.getName());

    private final Pastefy pastefy;
    private final Map<BackgroundJob.Type, BackgroundJobHandler> handlers = new EnumMap<>(BackgroundJob.Type.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService workers;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicInteger activeWorkers = new AtomicInteger(0);
    private final int workerCount;

    public BackgroundJobService(Pastefy pastefy) {
        this.pastefy = pastefy;
        workerCount = Math.max(1, pastefy.getConfig().getInt("ai.jobs.workers", 2));
        workers = Executors.newFixedThreadPool(workerCount);
    }

    public void register(BackgroundJob.Type type, BackgroundJobHandler handler) {
        handlers.put(type, handler);
    }

    public void start() {
        if (!started.compareAndSet(false, true)) return;

        long pollInterval = Math.max(1000, pastefy.getConfig().getInt("ai.jobs.pollintervalmillis", 5000));
        scheduler.scheduleWithFixedDelay(this::pollSafely, 0, pollInterval, TimeUnit.MILLISECONDS);
    }

    public void scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit) {
        scheduler.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    public synchronized void enqueue(BackgroundJob.Type type, int entityId, int sourceVersion, int promptVersion) {
        String key = type + ":" + entityId + ":" + sourceVersion + ":" + promptVersion;
        BackgroundJob existingJob = Repo.get(BackgroundJob.class).get(key);
        if (existingJob != null) {
            if (existingJob.status == BackgroundJob.Status.DONE) {
                Map<String, Object> update = new HashMap<>();
                update.put("status", BackgroundJob.Status.PENDING);
                update.put("attempts", 0);
                update.put("available_at", Timestamp.from(Instant.now()));
                update.put("lease_until", null);
                update.put("lease_token", null);
                update.put("last_error", null);
                Repo.get(BackgroundJob.class).where("key", key).update(update);
            }
            return;
        }

        BackgroundJob job = new BackgroundJob();
        job.key = key;
        job.type = type;
        job.entityId = entityId;
        job.sourceVersion = sourceVersion;
        job.promptVersion = promptVersion;
        job.availableAt = Timestamp.from(Instant.now());

        try {
            job.save();
        } catch (RuntimeException exception) {
            // Another instance may have inserted the deterministic key concurrently.
            if (Repo.get(BackgroundJob.class).get(key) == null) throw exception;
        }
    }

    private void pollSafely() {
        try {
            poll();
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Unable to poll background jobs", exception);
        }
    }

    private void poll() {
        int availableWorkers = workerCount - activeWorkers.get();
        for (int i = 0; i < availableWorkers; i++) {
            BackgroundJob job = claimNext();
            if (job == null) return;

            activeWorkers.incrementAndGet();
            workers.submit(() -> execute(job));
        }
    }

    private BackgroundJob claimNext() {
        Timestamp now = Timestamp.from(Instant.now());
        BackgroundJob job = Repo.get(BackgroundJob.class)
                .where("status", BackgroundJob.Status.PENDING)
                .where("availableAt", "<=", now)
                .first();

        if (job == null) {
            job = Repo.get(BackgroundJob.class)
                    .where("status", BackgroundJob.Status.RUNNING)
                    .whereNotNull("leaseUntil")
                    .where("leaseUntil", "<=", now)
                    .first();
        }

        if (job == null) return null;

        String leaseToken = UUID.randomUUID().toString();
        long leaseSeconds = Math.max(30, pastefy.getConfig().getInt("ai.jobs.leaseseconds", 120));
        Map<String, Object> update = new HashMap<>();
        update.put("status", BackgroundJob.Status.RUNNING);
        update.put("lease_until", Timestamp.from(Instant.now().plusSeconds(leaseSeconds)));
        update.put("lease_token", leaseToken);

        Query<BackgroundJob> claim = Repo.get(BackgroundJob.class)
                .where("key", job.key)
                .where("status", job.status);
        if (job.status == BackgroundJob.Status.PENDING) {
            claim.where("availableAt", "<=", now);
        } else {
            claim.where("leaseUntil", "<=", now);
        }
        claim.update(update);

        return Repo.get(BackgroundJob.class)
                .where("key", job.key)
                .where("leaseToken", leaseToken)
                .first();
    }

    private void execute(BackgroundJob job) {
        try {
            BackgroundJobHandler handler = handlers.get(job.type);
            if (handler == null) throw new IllegalStateException("No handler registered for job type " + job.type);

            handler.handle(job);
            complete(job);
        } catch (Exception exception) {
            fail(job, exception);
        } finally {
            activeWorkers.decrementAndGet();
        }
    }

    private void complete(BackgroundJob job) {
        Map<String, Object> update = new HashMap<>();
        update.put("status", BackgroundJob.Status.DONE);
        update.put("lease_until", null);
        update.put("lease_token", null);
        update.put("last_error", null);
        updateLeasedJob(job, update);
    }

    private void fail(BackgroundJob job, Exception exception) {
        int attempts = job.attempts + 1;
        int maxAttempts = Math.max(1, pastefy.getConfig().getInt("ai.jobs.maxattempts", 3));
        Map<String, Object> update = new HashMap<>();
        update.put("attempts", attempts);
        update.put("lease_until", null);
        update.put("lease_token", null);
        update.put("last_error", truncateError(exception));

        if (attempts >= maxAttempts) {
            update.put("status", BackgroundJob.Status.FAILED);
        } else {
            long baseDelaySeconds = Math.max(1, pastefy.getConfig().getInt("ai.jobs.retrydelayseconds", 60));
            int exponent = Math.min(attempts - 1, 10);
            update.put("status", BackgroundJob.Status.PENDING);
            update.put("available_at", Timestamp.from(Instant.now().plusSeconds(baseDelaySeconds * (1L << exponent))));
        }

        updateLeasedJob(job, update);
        LOGGER.log(Level.WARNING, "Background job failed: " + job.key, exception);
    }

    private void updateLeasedJob(BackgroundJob job, Map<String, Object> update) {
        Repo.get(BackgroundJob.class)
                .where("key", job.key)
                .where("leaseToken", job.leaseToken)
                .update(update);
    }

    private String truncateError(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) message = exception.getClass().getSimpleName();
        return message.length() > 2000 ? message.substring(0, 2000) : message;
    }
}
