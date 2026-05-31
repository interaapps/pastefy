package de.interaapps.pastefy.controller.public

import de.interaapps.pastefy.dto.pastes.PasteResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/public-pastes")
class PublicPastesController {
    @GetMapping
    fun getPublicPastes(): List<PasteResponse> {
        TODO()
    }

    @GetMapping("/trending")
    fun getTrendingPastes(): List<PasteResponse> {
        TODO()
    }

    @GetMapping("/latest")
    fun getLatestPastes(): List<PasteResponse> {
        TODO()
    }
}