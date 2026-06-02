package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.PasteComment
import org.springframework.data.jpa.repository.JpaRepository

interface PasteCommentRepository : JpaRepository<PasteComment, Int> {
    fun findAllByPaste(paste: String): List<PasteComment>
    fun deleteByPaste(paste: String)
    fun deleteByUserId(userId: String)
}
