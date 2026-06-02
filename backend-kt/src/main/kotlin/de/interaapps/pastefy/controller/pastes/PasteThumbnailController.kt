package de.interaapps.pastefy.controller.pastes

import de.interaapps.pastefy.auth.annotations.CurrentUser
import de.interaapps.pastefy.auth.annotations.LoginRequiredForRead
import de.interaapps.pastefy.auth.annotations.RateLimited
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PasteThumbnailController {
    @GetMapping("/{id}/thumbnail.png")
    @LoginRequiredForRead
    @RateLimited(limit = 3, windowMillis = 5_000)
    fun getThumbnail(@PathVariable id: String, @CurrentUser user: User?, response: HttpServletResponse) {
        response.contentType = MediaType.IMAGE_PNG_VALUE
        TODO()
    }
}
