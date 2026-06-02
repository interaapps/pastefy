package de.interaapps.pastefy.controller.pastes

import de.interaapps.pastefy.auth.annotations.CurrentUser
import de.interaapps.pastefy.auth.annotations.LoginRequiredForRead
import de.interaapps.pastefy.entities.User
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
    @LoginRequiredForRead
    fun getRaw(
        @PathVariable id: String,
        @CurrentUser user: User?,
    ): String = TODO()
}
