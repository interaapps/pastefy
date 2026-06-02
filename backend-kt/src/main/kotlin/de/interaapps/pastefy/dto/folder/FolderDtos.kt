package de.interaapps.pastefy.dto.folder

import de.interaapps.pastefy.dto.pastes.PasteResponse
import jakarta.validation.constraints.NotBlank

data class CreateFolderRequest(
    @field:NotBlank val name: String,
    val parent: String? = null,
)

data class FolderResponse(
    val exists: Boolean = false,
    val id: String? = null,
    val name: String? = null,
    val userId: String? = null,
    val children: List<FolderResponse>? = null,
    val pastes: List<PasteResponse>? = null,
    val created: String = "0000-00-00 00:00:00",
)

data class CreateFolderResponse(
    val folder: FolderResponse? = null,
    val success: Boolean = false,
)
