package de.interaapps.pastefy.controller

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.ActionResponse
import de.interaapps.pastefy.dto.folder.CreateFolderRequest
import de.interaapps.pastefy.dto.folder.CreateFolderResponse
import de.interaapps.pastefy.dto.folder.FolderResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.service.FolderService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/folder")
class FolderController(
    private val folders: FolderService,
) {
    @PostMapping
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("folders:create")
    fun createFolder(@Valid @RequestBody request: CreateFolderRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): CreateFolderResponse {
        val folder = folders.create(request, user)
        return CreateFolderResponse(folder = folders.map(folder), success = true)
    }

    @GetMapping
    @RequiresPermission("folders:read")
    fun getFolders(request: HttpServletRequest, @CurrentUser user: User?, @CurrentAuthKey authKey: AuthKey?): List<FolderResponse> =
        folders.list(request, user)

    @GetMapping("/{id}")
    @LoginRequiredForRead
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("folders:read")
    fun getFolder(@PathVariable id: String, request: HttpServletRequest, @CurrentUser user: User?, @CurrentAuthKey authKey: AuthKey?): FolderResponse {
        val folder = folders.get(id)
        return folders.map(
            folder,
            fetchChildren = true,
            fetchSubChildren = !request.hasParameter("hide_children"),
            fetchPastes = true,
            showPrivate = user?.id == folder.userId,
        )
    }

    @DeleteMapping("/{id}")
    @Authenticated
    @RejectAwaitingAccess
    @RejectBlocked
    @RequiresPermission("folders:delete")
    fun deleteFolder(@PathVariable id: String, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse {
        val folder = folders.get(id)
        if (folder.userId == user.id || user.isAdmin) {
            folders.delete(folder)
            return ActionResponse(success = true)
        }
        return ActionResponse()
    }
}

private fun HttpServletRequest.hasParameter(name: String) = parameterMap.containsKey(name)
