package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.PasteTag
import org.springframework.data.jpa.repository.JpaRepository

interface PasteTagRepository : JpaRepository<PasteTag, Int> {
    fun findAllByPaste(paste: String): List<PasteTag>
    fun countByTag(tag: String): Int
    fun deleteByPaste(paste: String)
}
