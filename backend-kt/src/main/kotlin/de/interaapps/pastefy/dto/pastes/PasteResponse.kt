package de.interaapps.pastefy.dto.pastes

import de.interaapps.pastefy.dto.user.PublicUserDto
import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility

data class PasteResponse(
    var id: String? = null,
    var content: String? = null,
    var title: String? = null,

    var visibility: PasteVisibility = PasteVisibility.UNLISTED,
    var type: PasteType = PasteType.PASTE,

    var createdAt: String = "0000-00-00 00:00:00",
    var encrypted: Boolean = false,
    var rawUrl: String? = null,

    var folder: String? = null,
    var userId: String? = null,
    var forkedFrom: String? = null,

    var expireAt: String? = null,
    var tags: List<String>? = null,

    var user: PublicUserDto? = null,

    var starred: Boolean? = null,
    var starCount: Long = 0,
    var commentCount: Long = 0,
    var viewCount: Long = 0,
    var aiInfo: PasteAiInfoResponse? = null,

    var exists: Boolean = true,
)
