package de.interaapps.pastefy.controller.pastes

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.ActionResponse
import de.interaapps.pastefy.dto.pastes.CreatePasteCommentRequest
import de.interaapps.pastefy.dto.pastes.PasteCommentMarkerResponse
import de.interaapps.pastefy.dto.pastes.PasteCommentResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/paste")
class PasteCommentsController {
    @GetMapping("/{pasteId}/comments")
    @LoginRequiredForRead
    @RejectAwaitingAccess
    @RejectBlocked
    fun getComments(@PathVariable pasteId: String, request: HttpServletRequest, @CurrentUser user: User?): List<PasteCommentResponse> = TODO()

    @GetMapping("/{pasteId}/comments/markers")
    @LoginRequiredForRead
    @RejectAwaitingAccess
    @RejectBlocked
    fun getLineMarkers(@PathVariable pasteId: String, @CurrentUser user: User?): List<PasteCommentMarkerResponse> = TODO()

    @PostMapping("/{pasteId}/comments")
    @RateLimited
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("comments:create")
    fun createComment(@PathVariable pasteId: String, @RequestBody request: CreatePasteCommentRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): PasteCommentResponse = TODO()

    @DeleteMapping("/{pasteId}/comments/{commentId}")
    @RateLimited
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("comments:delete")
    fun deleteComment(@PathVariable pasteId: String, @PathVariable commentId: Int, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()
}
