package de.interaapps.pastefy.service

import de.interaapps.pastefy.dto.pastes.CreatePasteCommentRequest
import de.interaapps.pastefy.dto.pastes.PasteCommentMarkerResponse
import de.interaapps.pastefy.dto.pastes.PasteCommentResponse
import de.interaapps.pastefy.entities.PasteComment
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.exceptions.HttpException
import de.interaapps.pastefy.exceptions.NotFoundException
import de.interaapps.pastefy.exceptions.PermissionsDeniedException
import de.interaapps.pastefy.repositories.PasteCommentRepository
import de.interaapps.pastefy.repositories.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PasteCommentService(
    private val pasteService: PasteService,
    private val commentRepository: PasteCommentRepository,
    private val userRepository: UserRepository,
) {
    fun list(pasteId: String, user: User?, page: Int, pageLimit: Int, line: Int?): List<PasteCommentResponse> {
        pasteService.getAccessiblePasteOrFail(pasteId, user)
        val pageable = PageRequest.of(page.coerceAtLeast(1) - 1, pageLimit.coerceIn(1, MAX_PAGE_LIMIT))
        val comments = if (line == null) {
            commentRepository.findAllByPasteAndParentIdIsNullOrderByCreatedAtDesc(pasteId, pageable)
        } else {
            commentRepository.findAllByPasteAndParentIdIsNullAndLineFromOrderByCreatedAtDesc(
                pasteId,
                line.coerceAtLeast(1),
                pageable
            )
        }
        return comments.map { map(it, fetchReplies = true) }
    }

    fun markers(pasteId: String, user: User?): List<PasteCommentMarkerResponse> {
        pasteService.getAccessiblePasteOrFail(pasteId, user)
        return commentRepository.findAllByPasteAndLineFromIsNotNullOrderByCreatedAtAsc(pasteId)
            .groupBy(PasteComment::lineFrom)
            .mapNotNull { (line, comments) ->
                line?.let {
                    val profiles = comments.mapNotNull { comment ->
                        userRepository.findById(comment.userId).orElse(null)?.toPublicDto()
                    }.distinctBy { profile -> profile.id }
                    PasteCommentMarkerResponse(it, profiles.take(2), (profiles.size - 2).coerceAtLeast(0))
                }
            }
    }

    @Transactional
    fun create(pasteId: String, request: CreatePasteCommentRequest, user: User): PasteCommentResponse {
        pasteService.getAccessiblePasteOrFail(pasteId, user)
        validate(request)
        val parentId = request.parentId?.let { id ->
            val parent = commentRepository.findById(id).orElseThrow(::NotFoundException)
            if (parent.paste != pasteId) throw NotFoundException()
            parent.parentId ?: parent.id
        }
        val saved = commentRepository.save(
            PasteComment(
                paste = pasteId,
                userId = user.id,
                content = requireNotNull(request.content).trim(),
                parentId = parentId,
                lineFrom = request.lineFrom,
                lineTo = request.lineTo,
            ),
        )
        return map(saved, fetchReplies = false)
    }

    @Transactional
    fun delete(pasteId: String, commentId: Int, user: User) {
        val paste = pasteService.get(pasteId) ?: throw NotFoundException()
        val comment = commentRepository.findById(commentId).orElseThrow(::NotFoundException)
        if (comment.paste != pasteId) throw NotFoundException()
        if (!user.isAdmin && user.id != comment.userId && user.id != paste.userId) throw PermissionsDeniedException()
        commentRepository.deleteByParentId(commentId)
        commentRepository.delete(comment)
    }

    fun validate(request: CreatePasteCommentRequest) {
        val content = request.content?.trim().orEmpty()
        if (content.isEmpty()) badRequest("Comment content is required")
        if (content.length > MAX_CONTENT_LENGTH) badRequest("Comment content must not exceed 2000 characters")
        if (request.lineFrom == null && request.lineTo != null) badRequest("line_to requires line_from")
        if (request.lineFrom != null && request.lineFrom < 1) badRequest("line_from must be positive")
        if (request.lineTo != null && request.lineTo < requireNotNull(request.lineFrom)) badRequest("line_to must not be smaller than line_from")
        if (request.lineTo != null && request.lineTo - requireNotNull(request.lineFrom) >= MAX_LINE_RANGE) {
            badRequest("Comment line ranges must not exceed 1000 lines")
        }
    }

    private fun map(comment: PasteComment, fetchReplies: Boolean): PasteCommentResponse =
        PasteCommentResponse(
            id = requireNotNull(comment.id),
            content = comment.content,
            parentId = comment.parentId,
            lineFrom = comment.lineFrom,
            lineTo = comment.lineTo,
            createdAt = comment.createdAt?.toString() ?: "0000-00-00 00:00:00",
            user = userRepository.findById(comment.userId).orElse(null)?.toPublicDto(),
            replies = if (fetchReplies) commentRepository.findAllByParentIdOrderByCreatedAtAsc(requireNotNull(comment.id))
                .map {
                    map(it, fetchReplies = false)
                } else emptyList(),
        )

    private fun badRequest(message: String): Nothing = throw HttpException(HttpStatus.BAD_REQUEST, message)

    companion object {
        const val DEFAULT_PAGE_LIMIT = 10
        const val MAX_PAGE_LIMIT = 30
        private const val MAX_CONTENT_LENGTH = 2_000
        private const val MAX_LINE_RANGE = 1_000
    }
}
