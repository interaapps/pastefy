package de.interaapps.pastefy.infrastructure.jobs

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.entities.BackgroundJob
import de.interaapps.pastefy.repositories.BackgroundJobRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@Service
@ConditionalOnProperty(prefix = "pastefy.ai", name = ["enabled"], havingValue = "true")
class BackgroundJobService(
    private val repository: BackgroundJobRepository,
    private val handlers: ObjectProvider<BackgroundJobHandler>,
    private val executor: ThreadPoolTaskExecutor,
    transactionManager: PlatformTransactionManager,
    private val properties: PastefyProperties,
) {
    private val transaction = TransactionTemplate(transactionManager)
    private val activeWorkers = AtomicInteger()

    fun enqueue(type: BackgroundJob.Type, entityId: Int, sourceVersion: Int?, promptVersion: Int) {
        transaction.executeWithoutResult {
            val key = "$type:$entityId:$sourceVersion:$promptVersion"
            val existing = repository.findById(key).orElse(null)
            if (existing != null) {
                if (existing.status == BackgroundJob.Status.DONE || existing.status == BackgroundJob.Status.FAILED) {
                    existing.status = BackgroundJob.Status.PENDING
                    existing.attempts = 0
                    existing.availableAt = Instant.now()
                    existing.leaseUntil = null
                    existing.leaseToken = null
                    existing.lastError = null
                }
                return@executeWithoutResult
            }
            try {
                repository.saveAndFlush(
                    BackgroundJob(
                        key = key,
                        type = type,
                        entityId = entityId,
                        sourceVersion = sourceVersion ?: 1,
                        promptVersion = promptVersion,
                        availableAt = Instant.now(),
                    ),
                )
            } catch (_: DataIntegrityViolationException) {
                // A second instance may have inserted the deterministic key concurrently.
            }
        }
    }

    @Scheduled(fixedDelayString = "\${pastefy.ai.jobs.poll-interval-millis:5000}")
    fun poll() {
        repeat((properties.ai.jobs.workers.coerceAtLeast(1) - activeWorkers.get()).coerceAtLeast(0)) {
            val job = claimNext() ?: return
            activeWorkers.incrementAndGet()
            executor.execute {
                try {
                    execute(job)
                } finally {
                    activeWorkers.decrementAndGet()
                }
            }
        }
    }

    private fun claimNext(): BackgroundJob? = transaction.execute {
        repository.findNextForUpdate(Instant.now())?.apply {
            status = BackgroundJob.Status.RUNNING
            leaseToken = UUID.randomUUID().toString()
            leaseUntil = Instant.now().plusSeconds(properties.ai.jobs.leaseSeconds.coerceAtLeast(30))
        }
    }

    private fun execute(job: BackgroundJob) {
        try {
            val handler = handlers.orderedStream().filter { it.type == job.type }.findFirst()
                .orElseThrow { IllegalStateException("No handler registered for job type ${job.type}") }
            handler.handle(job)
            updateLeased(job) {
                status = BackgroundJob.Status.DONE
                leaseUntil = null
                leaseToken = null
                lastError = null
            }
        } catch (exception: Exception) {
            fail(job, exception)
        }
    }

    private fun fail(job: BackgroundJob, exception: Exception) {
        updateLeased(job) {
            attempts += 1
            leaseUntil = null
            leaseToken = null
            lastError = (exception.message ?: exception.javaClass.simpleName).take(2_000)
            if (attempts >= properties.ai.jobs.maxAttempts.coerceAtLeast(1)) {
                status = BackgroundJob.Status.FAILED
            } else {
                status = BackgroundJob.Status.PENDING
                val exponent = (attempts - 1).coerceIn(0, 10)
                availableAt =
                    Instant.now().plusSeconds(properties.ai.jobs.retryDelaySeconds.coerceAtLeast(1) * (1L shl exponent))
            }
        }
        LOGGER.warn("Background job failed: {}", job.key, exception)
    }

    private fun updateLeased(job: BackgroundJob, update: BackgroundJob.() -> Unit) {
        transaction.executeWithoutResult {
            repository.findById(job.key).orElse(null)
                ?.takeIf { it.leaseToken == job.leaseToken }
                ?.apply(update)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(BackgroundJobService::class.java)
    }
}
