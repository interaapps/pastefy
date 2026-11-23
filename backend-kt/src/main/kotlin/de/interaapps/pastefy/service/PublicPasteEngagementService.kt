package de.interaapps.pastefy.service

import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.PublicPasteEngagement
import de.interaapps.pastefy.infrastructure.elastic.ElasticPasteService
import de.interaapps.pastefy.repositories.PublicPasteEngagementRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PublicPasteEngagementService(
    private val repository: PublicPasteEngagementRepository,
    private val elasticProvider: ObjectProvider<ElasticPasteService>,
) {
    @Async
    @Transactional
    fun addInterest(paste: Paste, score: Int) {
        val pasteId = requireNotNull(paste.id)
        runCatching {
            if (repository.incrementScore(pasteId, score) == 0) {
                repository.save(PublicPasteEngagement(pasteId = pasteId, score = score))
            }
            elasticProvider.ifAvailable?.updateEngagement(paste)
        }.onFailure {
            LOGGER.warn("Unable to update engagement for paste {}", pasteId, it)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PublicPasteEngagementService::class.java)
    }
}
