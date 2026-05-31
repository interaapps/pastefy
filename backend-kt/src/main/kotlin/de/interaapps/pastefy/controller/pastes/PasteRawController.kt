package de.interaapps.pastefy.controller.pastes

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PasteRawController {
    @GetMapping(
        "/{id}/raw",
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun getRaw(
        @PathVariable id: String,
        @PathVariable part: String? = null
    ): String {
        TODO()
    }
}