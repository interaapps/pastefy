package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.PasteStar
import org.springframework.data.jpa.repository.JpaRepository

interface PasteStarRepository : JpaRepository<PasteStar, Int> {

    fun existsByPasteAndUserId(paste: String, userId: String): Boolean

    fun deleteByPasteAndUserId(paste: String, userId: String)

    fun countByPaste(paste: String): Int

    fun findAllByPaste(paste: String): List<PasteStar>
    fun findAllByUserId(userId: String): List<PasteStar>

    fun deleteByPaste(paste: String)
}
