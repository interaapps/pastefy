package de.interaapps.pastefy.dto.pastes

data class CreatePasteResponse(
    val paste: PasteResponse,
    val success: Boolean = true
)
