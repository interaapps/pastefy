package de.interaapps.pastefy.controller.user

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.folder.FolderResponse
import de.interaapps.pastefy.dto.pastes.PasteResponse
import de.interaapps.pastefy.dto.user.UserPastesResponse
import de.interaapps.pastefy.dto.user.UserResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/user")
class UserController {
    @GetMapping
    fun getUser(@CurrentUser user: User?): UserResponse = TODO()

    @GetMapping("/overview")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission(allOf = ["pastes:read", "folders:read"])
    fun overview(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): UserPastesResponse = TODO()

    @GetMapping("/folders")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("folders:read")
    fun getFolders(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<FolderResponse> = TODO()

    @GetMapping("/pastes")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("pastes:read")
    fun getPastes(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<PasteResponse> = TODO()

    @GetMapping("/sharedpastes")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("sharedpastes:read")
    fun getSharedPastes(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<PasteResponse> = TODO()

    @GetMapping("/starred-pastes")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("stars:read")
    fun getStarredPastes(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<PasteResponse> = TODO()
}
