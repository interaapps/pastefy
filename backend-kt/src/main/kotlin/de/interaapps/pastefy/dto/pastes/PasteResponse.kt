package de.interaapps.pastefy.dto.pastes

import de.interaapps.pastefy.dto.user.PublicUserDto
import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility

data class PasteResponse(
    var id: String,
    var content: String,
    var title: String,

    var visibility: PasteVisibility,
    var type: PasteType,

    var createdAt: String = "0000-00-00 00:00:00",
    var encrypted: Boolean = false,
    var rawUrl: String? = null,

    var folder: String? = null,
    var userId: String? = null,
    var forkedFrom: String? = null,

    var expireAt: String? = null,
    var tags: MutableList<String?>? = null,

    var user: PublicUserDto? = null,

    var starred: Boolean? = null,

    var exists: Boolean = true,
    var success: Boolean = true,
) {
}