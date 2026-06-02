package de.interaapps.pastefy.infrastructure.ai

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.entities.AIWarning
import de.interaapps.pastefy.entities.BackgroundJob
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.PasteAIInfo
import de.interaapps.pastefy.infrastructure.jobs.BackgroundJobHandler
import de.interaapps.pastefy.infrastructure.jobs.BackgroundJobService
import de.interaapps.pastefy.repositories.PasteAIInfoRepository
import de.interaapps.pastefy.repositories.PasteRepository
import de.interaapps.pastefy.repositories.PublicPasteEngagementRepository
import de.interaapps.pastefy.service.PasteService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "pastefy.ai", name = ["enabled"], havingValue = "true")
class PasteAIInfoService(
    private val pasteAI: PasteAI,
    private val pasteService: PasteService,
    private val pasteRepository: PasteRepository,
    private val infoRepository: PasteAIInfoRepository,
    private val engagementRepository: PublicPasteEngagementRepository,
    private val jobs: BackgroundJobService,
    private val properties: PastefyProperties,
) : BackgroundJobHandler {
    override val type = BackgroundJob.Type.PASTE_AI_INFO

    fun enqueueIfEligible(paste: Paste, force: Boolean = false) {
        if (!force && !isEligible(paste)) return
        val pasteId = requireNotNull(paste.id)
        if (!force && isCurrent(infoRepository.findById(pasteId).orElse(null), paste.version)) return
        jobs.enqueue(type, pasteId, paste.version, PROMPT_VERSION)
    }

    @Scheduled(fixedDelayString = "\${pastefy.ai.jobs.sweep-interval-millis:300000}")
    fun sweep() {
        if (!properties.ai.jobs.sweeperEnabled) return
        try {
            engagementRepository.findAllByScoreGreaterThanEqual(threshold())
                .mapNotNull { pasteRepository.findById(it.pasteId).orElse(null) }
                .forEach(::enqueueIfEligible)
        } catch (exception: RuntimeException) {
            LOGGER.warn("Unable to enqueue eligible pastes for AI metadata generation", exception)
        }
    }

    override fun handle(job: BackgroundJob) {
        val paste = pasteRepository.findById(job.entityId).orElse(null) ?: return
        if (paste.version != job.sourceVersion) {
            enqueueIfEligible(paste)
            return
        }
        if (isCurrent(infoRepository.findById(job.entityId).orElse(null), paste.version)) return

        val generated = pasteAI.generateInfo(paste, pasteService.getContent(paste, withCache = false).orEmpty())
        val freshPaste = pasteRepository.findById(job.entityId).orElse(null) ?: return
        if (freshPaste.version != job.sourceVersion) {
            enqueueIfEligible(freshPaste)
            return
        }

        val warnings = generated.systemWarnings.asSequence()
            .map { AIWarning(it.description.trim().take(300), it.severity.coerceIn(1, 10)) }
            .filter { it.description.isNotBlank() }
            .take(10)
            .toMutableList()
        val info = infoRepository.findById(job.entityId).orElse(PasteAIInfo(pasteId = job.entityId))
        info.sourcePasteVersion = freshPaste.version
        info.promptVersion = PROMPT_VERSION
        info.provider = pasteAI.provider.take(30)
        info.model = pasteAI.model.take(100)
        info.description = generated.description.trim().take(500)
        info.tagsJson = generated.tags.asSequence()
            .map { it.trim().lowercase() }
            .filter(VALID_TAG::matches)
            .distinct()
            .take(8)
            .toMutableList()
        info.warningsJson = warnings
        info.dangerous = generated.dangerous
        info.maxSeverity = warnings.maxOfOrNull(AIWarning::severity) ?: 0
        info.suggestedFilename = generated.suggestedFilename?.trim()?.takeIf(String::isNotEmpty)?.take(255)
        info.generatedAt = java.time.Instant.now()
        infoRepository.save(info)
    }

    private fun isEligible(paste: Paste): Boolean =
        paste.isPublic && !paste.encrypted &&
            engagementRepository.findByPasteId(requireNotNull(paste.id))?.score.orZero() >= threshold()

    private fun isCurrent(info: PasteAIInfo?, sourceVersion: Int): Boolean =
        info != null && info.sourcePasteVersion == sourceVersion && info.promptVersion == PROMPT_VERSION &&
            info.provider == pasteAI.provider.take(30) && info.model == pasteAI.model.take(100)

    private fun threshold() = properties.ai.engagementThreshold.coerceAtLeast(1)
    private fun Int?.orZero() = this ?: 0

    companion object {
        private const val PROMPT_VERSION = 1
        private val VALID_TAG = Regex("^[a-z0-9-]{1,30}$")
        private val LOGGER = LoggerFactory.getLogger(PasteAIInfoService::class.java)
    }
}
