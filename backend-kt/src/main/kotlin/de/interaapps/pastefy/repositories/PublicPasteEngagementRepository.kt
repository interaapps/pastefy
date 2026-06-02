package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.PublicPasteEngagement
import org.springframework.data.jpa.repository.JpaRepository

interface PublicPasteEngagementRepository : JpaRepository<PublicPasteEngagement, Int> {
    fun findByPasteId(pasteId: Int): PublicPasteEngagement?
    fun deleteByPasteId(pasteId: Int)
    fun findAllByScoreGreaterThanEqual(score: Int): List<PublicPasteEngagement>
}
