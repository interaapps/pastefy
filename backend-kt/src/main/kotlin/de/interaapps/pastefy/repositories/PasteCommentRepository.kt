package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.PasteComment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable

interface PasteCommentRepository : JpaRepository<PasteComment, Int> {
    fun findAllByPaste(paste: String): List<PasteComment>
    fun findAllByPasteAndParentIdIsNullOrderByCreatedAtDesc(paste: String, pageable: Pageable): List<PasteComment>
    fun findAllByPasteAndParentIdIsNullAndLineFromOrderByCreatedAtDesc(paste: String, lineFrom: Int, pageable: Pageable): List<PasteComment>
    fun findAllByPasteAndLineFromIsNotNullOrderByCreatedAtAsc(paste: String): List<PasteComment>
    fun findAllByParentIdOrderByCreatedAtAsc(parentId: Int): List<PasteComment>
    fun deleteByParentId(parentId: Int)
    fun deleteByPaste(paste: String)
    fun deleteByUserId(userId: String)
}
