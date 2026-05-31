package de.interaapps.pastefy.dto.pastes

import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility

data class EditPasteRequest(
    val title: String,
    val content: String,

    val encrypted: Boolean? = false,
    val folder: String?,
    val expireAt: String?,
    val tags: List<String>?,
    val visibility: PasteVisibility? = PasteVisibility.UNLISTED,
    val type: PasteType? = PasteType.PASTE,
)
