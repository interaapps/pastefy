package de.interaapps.pastefy.service

import de.interaapps.pastefy.repositories.PasteRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ExpiredPasteCleanupService(
    private val pasteRepository: PasteRepository,
    private val pasteService: PasteService,
) {
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    fun deleteExpiredPastes() {
        pasteRepository.findAllByExpireAtBeforeAndExpireAtIsNotNull(Instant.now()).forEach { paste ->
            runCatching {
                pasteService.delete(paste)
            }.onFailure {
                LOGGER.warn("Unable to delete expired paste {}", paste.key, it)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ExpiredPasteCleanupService::class.java)
    }
}
