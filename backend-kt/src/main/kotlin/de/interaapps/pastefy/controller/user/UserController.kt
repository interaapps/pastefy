package de.interaapps.pastefy.controller.user

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.folder.FolderResponse
import de.interaapps.pastefy.dto.pastes.PasteResponse
import de.interaapps.pastefy.dto.user.UserPastesResponse
import de.interaapps.pastefy.dto.user.UserResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import de.interaapps.pastefy.repositories.PasteRepository
import de.interaapps.pastefy.repositories.SharedPasteRepository
import de.interaapps.pastefy.service.FolderService
import de.interaapps.pastefy.service.PasteQueryService
import de.interaapps.pastefy.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/user")
class UserController(
    private val users: UserService,
    private val folders: FolderService,
    private val queries: PasteQueryService,
    private val pasteRepository: PasteRepository,
    private val sharedPasteRepository: SharedPasteRepository,
) {
    @GetMapping
    fun getUser(@CurrentUser user: User?): UserResponse = user?.let {
        UserResponse(
            loggedIn = true,
            id = it.id,
            name = it.uniqueName,
            displayName = it.name,
            color = "#f52966",
            profilePicture = it.avatar,
            authType = it.authProvider?.providerName,
            authTypes = listOfNotNull(it.authProvider?.providerName),
            type = it.type,
        )
    } ?: UserResponse()

    @GetMapping("/overview")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission(allOf = ["pastes:read", "folders:read"])
    fun overview(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): UserPastesResponse {
        val page = request.getParameter("page")?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        return UserPastesResponse(
            pastes = pasteRepository.findAllByUserIdAndFolderIsNullOrderByUpdatedAtDesc(user.id, PageRequest.of(page - 1, 10))
                .map { queries.map(it, request, user) },
            folder = foldersForUser(request, user),
        )
    }

    @GetMapping("/folders")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("folders:read")
    fun getFolders(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<FolderResponse> =
        foldersForUser(request, user)

    @GetMapping("/pastes")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("pastes:read")
    fun getPastes(request: HttpServletRequest, response: HttpServletResponse, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<PasteResponse> =
        queries.list(request, response, user, guarded = false, userId = user.id)

    @GetMapping("/sharedpastes")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("sharedpastes:read")
    fun getSharedPastes(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<PasteResponse> {
        val page = request.getParameter("page")?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        return sharedPasteRepository.findAllByTargetIdOrderByUpdatedAtDesc(user.id, PageRequest.of(page - 1, 10)).mapNotNull { shared ->
            pasteRepository.findByKey(shared.paste)?.let { queries.map(it, request, user) } ?: run {
                sharedPasteRepository.delete(shared)
                null
            }
        }
    }

    @GetMapping("/starred-pastes")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("stars:read")
    fun getStarredPastes(request: HttpServletRequest, response: HttpServletResponse, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<PasteResponse> =
        queries.list(request, response, user, guarded = false, starredBy = user.id)

    private fun foldersForUser(request: HttpServletRequest, user: User): List<FolderResponse> =
        users.getFolders(user).filter { it.parent == null }.map {
            folders.map(
                it,
                fetchChildren = !request.parameterMap.containsKey("hide_children"),
                fetchSubChildren = !request.parameterMap.containsKey("hide_sub_children"),
                fetchPastes = !request.parameterMap.containsKey("hide_pastes"),
                showPrivate = true,
            )
        }
}
