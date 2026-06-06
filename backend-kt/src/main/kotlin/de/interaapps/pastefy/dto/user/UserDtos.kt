package de.interaapps.pastefy.dto.user

import de.interaapps.pastefy.dto.folder.FolderResponse
import de.interaapps.pastefy.dto.pastes.PasteResponse
import de.interaapps.pastefy.entities.User
import jakarta.validation.constraints.Size

data class UserResponse(
    val loggedIn: Boolean = false,
    val id: String? = null,
    val name: String? = null,
    val displayName: String? = null,
    val color: String? = null,
    val profilePicture: String? = null,
    val authType: String? = null,
    val authTypes: List<String> = emptyList(),
    val type: User.Type? = null,
)

data class UserPastesResponse(
    val pastes: List<PasteResponse> = emptyList(),
    val folder: List<FolderResponse> = emptyList(),
)

data class CreateAuthKeyResponse(
    val key: String? = null,
    val success: Boolean = false,
)

data class EditUserRequest(
    @field:Size(min = 2, max = 255) val name: String? = null,
    @field:Size(min = 2, max = 33) val uniqueName: String? = null,
    val type: User.Type? = null,
)
