package de.interaapps.pastefy.ai;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.jobs.BackgroundJobService;
import de.interaapps.pastefy.model.database.BackgroundJob;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.PasteAIInfo;
import de.interaapps.pastefy.model.database.algorithm.PublicPasteEngagement;
import org.javawebstack.abstractdata.AbstractArray;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.orm.Repo;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PasteAIInfoService {
    private static final Logger LOGGER = Logger.getLogger(PasteAIInfoService.class.getName());
    private static final int PROMPT_VERSION = 1;
    private static final Pattern VALID_TAG_PATTERN = Pattern.compile("^[a-z0-9-]{1,30}$");

    private final Pastefy pastefy;
    private final BackgroundJobService jobs;

    public PasteAIInfoService(Pastefy pastefy, BackgroundJobService jobs) {
        this.pastefy = pastefy;
        this.jobs = jobs;
        jobs.register(BackgroundJob.Type.PASTE_AI_INFO, this::handle);
    }

    public void start() {
        if (!pastefy.aiEnabled()
                || !pastefy.getConfig().get("ai.jobs.sweeperenabled", "false").equalsIgnoreCase("true")) return;

        long sweepInterval = Math.max(60_000, pastefy.getConfig().getInt("ai.jobs.sweepintervalmillis", 300_000));
        jobs.scheduleWithFixedDelay(this::sweepSafely, 0, sweepInterval, TimeUnit.MILLISECONDS);
    }

    public void enqueueIfEligible(Paste paste, boolean force) {
        if ((!pastefy.aiEnabled() || !isEligible(paste)) && !force) return;

        int sourceVersion = paste.getVersion() == null ? 0 : paste.getVersion();
        PasteAIInfo current = Repo.get(PasteAIInfo.class).get(paste.getId());
        if (isCurrent(current, sourceVersion) && !force) return;

        jobs.enqueue(BackgroundJob.Type.PASTE_AI_INFO, paste.getId(), sourceVersion, PROMPT_VERSION);
    }

    public void enqueueIfEligible(Paste paste) {
        enqueueIfEligible(paste, false);
    }

    public void enqueue(Paste paste) {
        enqueueIfEligible(paste, true);
    }


    private void sweepSafely() {
        try {
            sweep();
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Unable to enqueue eligible pastes for AI metadata generation", exception);
        }
    }

    private void sweep() {
        int threshold = getEngagementThreshold();
        Repo.get(PublicPasteEngagement.class)
                .where("score", ">=", threshold)
                .all()
                .forEach(engagement -> {
                    Paste paste = Repo.get(Paste.class).where("id", engagement.pasteId).first();
                    enqueueIfEligible(paste);
                });
    }

    private void handle(BackgroundJob job) {
        Paste paste = Repo.get(Paste.class).where("id", job.entityId).first();
        //if (!isEligible(paste)) return;

        int sourceVersion = paste.getVersion() == null ? 0 : paste.getVersion();
        if (sourceVersion != job.sourceVersion) {
            enqueueIfEligible(paste);
            return;
        }

        PasteAIInfo current = Repo.get(PasteAIInfo.class).get(paste.getId());
        if (isCurrent(current, sourceVersion)) return;

        AbstractObject generated = pastefy.getPasteAI().generateInfo(paste);

        Paste freshPaste = Repo.get(Paste.class).where("id", paste.getId()).first();
        int freshVersion = freshPaste.getVersion() == null ? 0 : freshPaste.getVersion();
        if (freshVersion != sourceVersion) {
            enqueueIfEligible(freshPaste);
            return;
        }

        saveInfo(freshPaste, generated);
    }

    private void saveInfo(Paste paste, AbstractObject generated) {
        AbstractArray tags = sanitizeTags(generated.array("tags", new AbstractArray()));
        AbstractArray warnings = sanitizeWarnings(generated.array("system_warnings", new AbstractArray()));

        PasteAIInfo info = Repo.get(PasteAIInfo.class).get(paste.getId());
        if (info == null) info = new PasteAIInfo();

        info.pasteId = paste.getId();
        info.sourcePasteVersion = paste.getVersion() == null ? 0 : paste.getVersion();
        info.promptVersion = PROMPT_VERSION;
        info.provider = pastefy.getPasteAI().getProviderName();
        info.model = pastefy.getPasteAI().getModel();
        info.description = truncate(generated.string("description", "").trim(), 500);
        info.tagsJson = tags;
        info.warningsJson = warnings;
        info.dangerous = generated.bool("dangerous", false);
        info.maxSeverity = warnings.stream()
                .map(AbstractElement::object)
                .mapToInt(warning -> warning.number("severity", 0).intValue())
                .max()
                .orElse(0);
        info.suggestedFilename = optionalTruncated(generated.string("suggested_filename", ""), 255);
        info.generatedAt = Timestamp.from(Instant.now());
        info.save();
    }

    private AbstractArray sanitizeTags(AbstractArray source) {
        AbstractArray tags = new AbstractArray();
        Set<String> addedTags = new HashSet<>();
        source.stream()
                .filter(AbstractElement::isString)
                .map(AbstractElement::string)
                .map(String::trim)
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .filter(VALID_TAG_PATTERN.asPredicate())
                .filter(addedTags::add)
                .limit(8)
                .forEach(tags::add);
        return tags;
    }

    private AbstractArray sanitizeWarnings(AbstractArray source) {
        AbstractArray warnings = new AbstractArray();
        source.stream()
                .filter(AbstractElement::isObject)
                .map(AbstractElement::object)
                .limit(10)
                .forEach(warning -> {
                    String description = truncate(warning.string("description", "").trim(), 300);
                    if (description.isEmpty()) return;

                    int severity = Math.max(1, Math.min(10, warning.number("severity", 1).intValue()));
                    warnings.add(new AbstractObject()
                            .set("description", description)
                            .set("severity", severity));
                });
        return warnings;
    }

    private boolean isEligible(Paste paste) {
        return paste != null
                && paste.isPublic()
                && !paste.isEncrypted()
                && paste.getEngagementScore() >= getEngagementThreshold();
    }

    private boolean isCurrent(PasteAIInfo info, int sourceVersion) {
        return info != null
                && info.sourcePasteVersion == sourceVersion
                && info.promptVersion == PROMPT_VERSION
                && Objects.equals(info.provider, pastefy.getPasteAI().getProviderName())
                && Objects.equals(info.model, pastefy.getPasteAI().getModel());
    }

    private int getEngagementThreshold() {
        return Math.max(1, pastefy.getConfig().getInt("ai.engagement.threshold", 100));
    }

    private String optionalTruncated(String value, int maxLength) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : truncate(trimmed, maxLength);
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) return value;
        return value.substring(0, maxLength);
    }
}
