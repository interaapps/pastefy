package de.interaapps.pastefy.controller.pastes

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.interaapps.pastefy.auth.annotations.CurrentUser
import de.interaapps.pastefy.auth.annotations.LoginRequiredForRead
import de.interaapps.pastefy.dto.pastes.MultiPastesElement
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.infrastructure.analytics.AnalyticsService
import de.interaapps.pastefy.service.PasteService
import de.interaapps.pastefy.service.PublicPasteEngagementService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PasteRawController(
    private val pasteService: PasteService,
    private val engagement: PublicPasteEngagementService,
    private val analyticsProvider: ObjectProvider<AnalyticsService>,
    private val objectMapper: ObjectMapper,
) {
    @GetMapping(
        "/{id}/raw",
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    @LoginRequiredForRead
    fun getRaw(
        @PathVariable id: String,
        @CurrentUser user: User?,
        request: HttpServletRequest,
    ): ResponseEntity<String> {
        val paste = pasteService.get(id) ?: return text("404 - Paste not found", HttpStatus.NOT_FOUND)

        pasteService.getAccessiblePasteOrFail(id, user)

        if (paste.isPublic) engagement.addInterest(paste, 1)

        analyticsProvider.ifAvailable?.track(request, paste, user, AnalyticsService.VisitType.RAW)

        val content = pasteService.getContent(paste).orEmpty()

        val part = request.getParameter("part")

        if (part != null && paste.type == PasteType.MULTI_PASTE) {
            val contents = objectMapper.readValue(content, object : TypeReference<List<MultiPastesElement>>() {})
                .firstOrNull { it.name == part }?.contents
                ?: return text("404 - Paste part not found", HttpStatus.NOT_FOUND)
            return text(contents)
        }

        return text(content)
    }

    private fun text(body: String, status: HttpStatus = HttpStatus.OK): ResponseEntity<String> =
        ResponseEntity.status(status)
            .contentType(MediaType("text", "plain", Charsets.UTF_8))
            .body(body)
}
