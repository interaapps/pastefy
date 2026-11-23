package de.interaapps.pastefy.dto.pastes

import de.interaapps.pastefy.entities.AIWarning

import de.interaapps.pastefy.dto.user.PublicUserDto

data class CreatePasteCommentRequest(
    val content: String? = null,
    val parentId: Int? = null,
    val lineFrom: Int? = null,
    val lineTo: Int? = null,
)

data class PasteCommentResponse(
    val id: Int,
    val content: String,
    val parentId: Int? = null,
    val lineFrom: Int? = null,
    val lineTo: Int? = null,
    val createdAt: String,
    val user: PublicUserDto? = null,
    val replies: List<PasteCommentResponse> = emptyList(),
)

data class PasteCommentMarkerResponse(
    val line: Int,
    val profiles: List<PublicUserDto> = emptyList(),
    val additionalProfiles: Int = 0,
)

data class AddFriendToPasteRequest(
    val friend: String,
)

data class MultiPastesElement(
    val name: String,
    val contents: String,
)

data class PasteAiInfoResponse(
    val dangerous: Boolean = false,
    val suggestedFilename: String? = null,
    val warnings: List<AIWarning>? = null,
    val tags: List<String>? = null,
    val description: String? = null,
)
