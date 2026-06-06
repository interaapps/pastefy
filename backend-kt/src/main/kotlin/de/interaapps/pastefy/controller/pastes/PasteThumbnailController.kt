package de.interaapps.pastefy.controller.pastes

import de.interaapps.pastefy.auth.annotations.CurrentUser
import de.interaapps.pastefy.auth.annotations.LoginRequiredForRead
import de.interaapps.pastefy.auth.annotations.RateLimited
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.exceptions.NotFoundException
import de.interaapps.pastefy.service.PasteService
import de.interaapps.pastefy.service.PasteThumbnailService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PasteThumbnailController(
    private val pasteService: PasteService,
    private val thumbnails: PasteThumbnailService,
) {
    @GetMapping("/{id}/thumbnail.png")
    @LoginRequiredForRead
    @RateLimited(limit = 3, windowMillis = 5_000)
    fun getThumbnail(@PathVariable id: String, @CurrentUser user: User?, response: HttpServletResponse) {
        val paste = pasteService.get(id) ?: throw NotFoundException()
        if (paste.isPrivate || paste.encrypted) throw NotFoundException()
        val bytes = thumbnails.render(paste.title, pasteService.getContent(paste).orEmpty())
        response.contentType = MediaType.IMAGE_PNG_VALUE
        response.setContentLength(bytes.size)
        response.outputStream.use { it.write(bytes) }
    }
}
