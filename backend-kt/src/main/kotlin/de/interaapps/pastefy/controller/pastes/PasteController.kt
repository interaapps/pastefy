package de.interaapps.pastefy.controller.pastes

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.ActionResponse
import de.interaapps.pastefy.dto.pastes.AddFriendToPasteRequest
import de.interaapps.pastefy.dto.pastes.CreatePasteRequest
import de.interaapps.pastefy.dto.pastes.CreatePasteResponse
import de.interaapps.pastefy.dto.pastes.EditPasteRequest
import de.interaapps.pastefy.dto.pastes.PasteResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/paste")
class PasteController {
    @PostMapping
    @RateLimited
    @LoginRequiredForCreate
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("pastes:create", "pastes:write")
    fun createPaste(
        @Valid @RequestBody request: CreatePasteRequest,
        @CurrentUser user: User?,
        @CurrentAuthKey authKey: AuthKey?,
    ): CreatePasteResponse = TODO()

    @GetMapping
    @RequiresPermission("pastes:read")
    fun getPastes(
        request: HttpServletRequest,
        @CurrentUser user: User?,
        @CurrentAuthKey authKey: AuthKey?,
    ): List<PasteResponse> = TODO()

    @GetMapping("/{id}")
    @LoginRequiredForRead
    @RejectAwaitingAccess
    @RejectBlocked
    fun getPaste(
        @PathVariable id: String,
        @RequestParam fromFrontend: Boolean = false,
        @CurrentUser user: User?,
    ): PasteResponse = TODO()

    @PutMapping("/{id}")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("pastes:edit", "pastes:write")
    fun putPaste(
        @PathVariable id: String,
        @Valid @RequestBody request: EditPasteRequest,
        @CurrentUser user: User,
        @CurrentAuthKey authKey: AuthKey,
    ): ActionResponse = TODO()

    @DeleteMapping("/{id}")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("pastes:delete")
    fun deletePaste(@PathVariable id: String, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()

    @PostMapping("/{id}/star")
    @Authenticated
    @LoginRequiredForRead
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("stars:create")
    fun addStarToPaste(@PathVariable id: String, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()

    @DeleteMapping("/{id}/star")
    @Authenticated
    @LoginRequiredForRead
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("stars:delete")
    fun removeStarFromPaste(@PathVariable id: String, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()

    @PostMapping("/{id}/friend")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    fun addFriend(@PathVariable id: String, @RequestBody request: AddFriendToPasteRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()

    @PostMapping("/{id}/ai-analysis")
    @AdminRoute
    @RequiresPermission("pastes.ai_analysis:create")
    fun createAiAnalysisJob(@PathVariable id: String, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()
}
