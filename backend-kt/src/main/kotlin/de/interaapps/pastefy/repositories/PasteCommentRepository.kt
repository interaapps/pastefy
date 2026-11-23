package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.PasteComment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PasteCommentRepository : JpaRepository<PasteComment, Int> {
    fun findAllByPaste(paste: String): List<PasteComment>
    fun findAllByPasteAndParentIdIsNullOrderByCreatedAtDesc(paste: String, pageable: Pageable): List<PasteComment>
    fun findAllByPasteAndParentIdIsNullAndLineFromOrderByCreatedAtDesc(
        paste: String,
        lineFrom: Int,
        pageable: Pageable
    ): List<PasteComment>

    fun findAllByPasteAndLineFromIsNotNullOrderByCreatedAtAsc(paste: String): List<PasteComment>
    fun findAllByParentIdOrderByCreatedAtAsc(parentId: Int): List<PasteComment>

    @Query("select c.paste as paste, count(c) as count from PasteComment c where c.paste in :pastes group by c.paste")
    fun countGroupedByPaste(@Param("pastes") pastes: Collection<String>): List<PasteCountProjection>

    fun deleteByParentId(parentId: Int)
    fun deleteByPaste(paste: String)
    fun deleteByUserId(userId: String)
}
