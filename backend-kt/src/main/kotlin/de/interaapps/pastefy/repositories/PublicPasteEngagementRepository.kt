package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.PublicPasteEngagement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PublicPasteEngagementRepository : JpaRepository<PublicPasteEngagement, Int> {
    fun findByPasteId(pasteId: Int): PublicPasteEngagement?
    fun deleteByPasteId(pasteId: Int)
    fun findAllByScoreGreaterThanEqual(score: Int): List<PublicPasteEngagement>

    @Modifying
    @Query(
        value = """
            update pastefy_public_paste_engagements
            set score = score + :score, updated_at = current_timestamp()
            where paste_id = :pasteId
        """,
        nativeQuery = true,
    )
    fun incrementScore(@Param("pasteId") pasteId: Int, @Param("score") score: Int): Int
}
