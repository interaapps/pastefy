package de.interaapps.pastefy.dto.pastes

data class CreatePasteResponse(
    val paste: PasteResponse? = null,
    val success: Boolean = true
)
