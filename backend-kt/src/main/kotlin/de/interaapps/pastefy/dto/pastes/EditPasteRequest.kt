package de.interaapps.pastefy.dto.pastes

import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility

data class EditPasteRequest(
    val title: String? = null,
    val content: String? = null,
    val encrypted: Boolean? = null,
    val folder: String? = null,
    val expireAt: String? = null,
    val tags: List<String>? = null,
    val visibility: PasteVisibility? = null,
    val type: PasteType? = null,
)
