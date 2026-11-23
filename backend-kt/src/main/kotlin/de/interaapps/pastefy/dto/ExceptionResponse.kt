package de.interaapps.pastefy.dto

data class ExceptionResponse(
    val exception: String,
    val message: String?,
    val success: Boolean = false,
    val error: Boolean = true,
    val exists: Boolean = false,
)
