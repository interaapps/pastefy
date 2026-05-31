package de.interaapps.pastefy.controller.pastes

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PasteThumbnailController {
    @GetMapping("/{id}/thumbnail.png")
    fun getThumbnail(@PathVariable id: String, response: HttpServletResponse) {
        response.contentType = MediaType.IMAGE_PNG_VALUE
        TODO()
    }
}