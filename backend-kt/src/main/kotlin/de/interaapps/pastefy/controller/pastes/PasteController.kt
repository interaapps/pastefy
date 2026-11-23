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
import de.interaapps.pastefy.exceptions.FeatureDisabledException
import de.interaapps.pastefy.infrastructure.ai.PasteAIInfoService
import de.interaapps.pastefy.infrastructure.analytics.AnalyticsService
import de.interaapps.pastefy.service.PasteCommandService
import de.interaapps.pastefy.service.PasteQueryService
import de.interaapps.pastefy.service.PasteService
import de.interaapps.pastefy.service.PublicPasteEngagementService
import de.interaapps.pastefy.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import org.springframework.beans.factory.ObjectProvider

@RestController
@RequestMapping("/api/v2/paste")
class PasteController(
    private val commands: PasteCommandService,
    private val queries: PasteQueryService,
    private val pasteService: PasteService,
    private val users: UserService,
    private val engagement: PublicPasteEngagementService,
    private val analyticsProvider: ObjectProvider<AnalyticsService>,
    private val aiProvider: ObjectProvider<PasteAIInfoService>,
) {
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
        servletRequest: HttpServletRequest,
    ): CreatePasteResponse =
        CreatePasteResponse(paste = queries.map(commands.create(request, user), servletRequest, user), success = true)

    @GetMapping
    @RequiresPermission("pastes:read")
    fun getPastes(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @CurrentUser user: User?,
        @CurrentAuthKey authKey: AuthKey?,
    ): List<PasteResponse> = queries.list(request, response, user)

    @GetMapping("/{id}")
    @LoginRequiredForRead
    @RejectAwaitingAccess
    @RejectBlocked
    fun getPaste(
        @PathVariable id: String,
        @RequestParam("from_frontend", defaultValue = "false") fromFrontend: Boolean,
        @CurrentUser user: User?,
        request: HttpServletRequest,
    ): PasteResponse {
        val paste = pasteService.getAccessiblePasteOrFail(id, user)
        if (paste.isPublic && user?.id != paste.userId) {
            engagement.addInterest(paste, if (fromFrontend) if (user == null) 5 else 4 else 2)
        }
        if (fromFrontend) analyticsProvider.ifAvailable?.track(request, paste, user, AnalyticsService.VisitType.PAGE)
        return queries.map(paste, request, user)
    }

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
    ): ActionResponse {
        commands.update(id, request, user)
        return ActionResponse(success = true)
    }

    @DeleteMapping("/{id}")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("pastes:delete")
    fun deletePaste(
        @PathVariable id: String,
        @CurrentUser user: User,
        @CurrentAuthKey authKey: AuthKey
    ): ActionResponse {
        commands.delete(id, user)
        return ActionResponse(success = true)
    }

    @PostMapping("/{id}/star")
    @Authenticated
    @LoginRequiredForRead
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("stars:create")
    fun addStarToPaste(
        @PathVariable id: String,
        @CurrentUser user: User,
        @CurrentAuthKey authKey: AuthKey
    ): ActionResponse {
        users.star(user, pasteService.getAccessiblePasteOrFail(id, user))
        return ActionResponse(success = true)
    }

    @DeleteMapping("/{id}/star")
    @Authenticated
    @LoginRequiredForRead
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("stars:delete")
    fun removeStarFromPaste(
        @PathVariable id: String,
        @CurrentUser user: User,
        @CurrentAuthKey authKey: AuthKey
    ): ActionResponse {
        users.unstar(user, pasteService.getAccessiblePasteOrFail(id, user))
        return ActionResponse(success = true)
    }

    @PostMapping("/{id}/friend")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    fun addFriend(
        @PathVariable id: String,
        @RequestBody request: AddFriendToPasteRequest,
        @CurrentUser user: User,
        @CurrentAuthKey authKey: AuthKey
    ): ActionResponse =
        throw UnsupportedOperationException("NOT IMPLEMENTED")

    @PostMapping("/{id}/ai-analysis")
    @AdminRoute
    @RequiresPermission("pastes.ai_analysis:create")
    fun createAiAnalysisJob(
        @PathVariable id: String,
        @CurrentUser user: User,
        @CurrentAuthKey authKey: AuthKey
    ): ActionResponse {
        val ai = aiProvider.ifAvailable ?: throw FeatureDisabledException("AI features are disabled")
        ai.enqueueIfEligible(pasteService.getAccessiblePasteOrFail(id, user), force = true)
        return ActionResponse(success = true)
    }
}
