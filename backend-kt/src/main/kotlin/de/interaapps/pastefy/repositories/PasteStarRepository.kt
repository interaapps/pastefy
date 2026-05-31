package de.interaapps.pastefy.repositories

import org.springframework.data.jpa.repository.JpaRepository

interface PasteStarRepository : JpaRepository<PasteStar, Int> {

    fun existsByPasteAndUserId(paste: String, userId: String): Boolean

    fun deleteByPasteAndUserId(paste: String, userId: String)

    fun countByPaste(paste: String): Int
}