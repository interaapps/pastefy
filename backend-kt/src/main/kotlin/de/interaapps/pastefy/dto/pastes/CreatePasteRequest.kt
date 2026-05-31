package de.interaapps.pastefy.dto.pastes

import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility
import jakarta.validation.constraints.NotBlank

data class CreatePasteRequest(
    val title: String,
    @field:NotBlank
    val content: String,

    val encrypted: Boolean? = false,
    val folder: String?,
    val expireAt: String?,
    val forkedFrom: String?,
    val tags: List<String>?,
    val visibility: PasteVisibility? = PasteVisibility.UNLISTED,
    val type: PasteType? = PasteType.PASTE,
)
