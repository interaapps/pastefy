package de.interaapps.pastefy.controller.public

import de.interaapps.pastefy.auth.annotations.PublicPastesEnabled
import de.interaapps.pastefy.dto.pastes.PasteResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/public-pastes")
@PublicPastesEnabled
class PublicPastesController {
    @GetMapping
    fun getPublicPastes(request: HttpServletRequest): List<PasteResponse> = TODO()

    @GetMapping("/trending")
    fun getTrendingPastes(request: HttpServletRequest): List<PasteResponse> = TODO()

    @GetMapping("/latest")
    fun getLatestPastes(request: HttpServletRequest): List<PasteResponse> = TODO()
}
