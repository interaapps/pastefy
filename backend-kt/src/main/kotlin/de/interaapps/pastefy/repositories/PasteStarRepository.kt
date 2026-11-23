package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.PasteStar
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PasteStarRepository : JpaRepository<PasteStar, Int> {

    fun existsByPasteAndUserId(paste: String, userId: String): Boolean

    fun deleteByPasteAndUserId(paste: String, userId: String)

    fun countByPaste(paste: String): Int

    @Query("select s.paste as paste, count(s) as count from PasteStar s where s.paste in :pastes group by s.paste")
    fun countGroupedByPaste(@Param("pastes") pastes: Collection<String>): List<PasteCountProjection>

    fun findAllByPaste(paste: String): List<PasteStar>
    fun findAllByUserId(userId: String): List<PasteStar>

    fun deleteByPaste(paste: String)
}

interface PasteCountProjection {
    val paste: String
    val count: Long
}
