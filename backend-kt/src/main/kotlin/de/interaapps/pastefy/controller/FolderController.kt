package de.interaapps.pastefy.controller

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.ActionResponse
import de.interaapps.pastefy.dto.folder.CreateFolderRequest
import de.interaapps.pastefy.dto.folder.CreateFolderResponse
import de.interaapps.pastefy.dto.folder.FolderResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/folder")
class FolderController {
    @PostMapping
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("folders:create")
    fun createFolder(@Valid @RequestBody request: CreateFolderRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): CreateFolderResponse = TODO()

    @GetMapping
    @RequiresPermission("folders:read")
    fun getFolders(request: HttpServletRequest, @CurrentUser user: User?, @CurrentAuthKey authKey: AuthKey?): List<FolderResponse> = TODO()

    @GetMapping("/{id}")
    @LoginRequiredForRead
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("folders:read")
    fun getFolder(@PathVariable id: String, request: HttpServletRequest, @CurrentUser user: User?, @CurrentAuthKey authKey: AuthKey?): FolderResponse = TODO()

    @DeleteMapping("/{id}")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("folders:delete")
    fun deleteFolder(@PathVariable id: String, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()
}
