package de.interaapps.pastefy.controller.public

import de.interaapps.pastefy.auth.annotations.PublicPastesEnabled
import de.interaapps.pastefy.dto.pastes.PasteResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.service.PasteQueryService
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/public-pastes")
@PublicPastesEnabled
class PublicPastesController(
    private val queries: PasteQueryService,
) {
    @GetMapping
    //@Cacheable()
    fun getPublicPastes(request: HttpServletRequest, response: HttpServletResponse): List<PasteResponse> =
        queries.list(request, response, null, guarded = false, visibility = PasteVisibility.PUBLIC)

    @GetMapping("/trending")
    fun getTrendingPastes(request: HttpServletRequest, response: HttpServletResponse): List<PasteResponse> =
        queries.trending(request, response)

    @GetMapping("/latest")
    fun getLatestPastes(request: HttpServletRequest, response: HttpServletResponse): List<PasteResponse> =
        queries.list(request, response, null, guarded = false, visibility = PasteVisibility.PUBLIC)
}
