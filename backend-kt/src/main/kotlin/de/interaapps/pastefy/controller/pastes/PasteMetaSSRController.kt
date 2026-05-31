package de.interaapps.pastefy.controller.pastes

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PasteMetaSSRController {
    @GetMapping("/{id}")
    fun getPasteMetaSSR(@PathVariable id: String): String {
        TODO()
    }
}