package de.interaapps.pastefy.controller.seo

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tags")
class TagMetaController {
    @GetMapping("/{tag}")
    fun tagMeta(@PathVariable tag: String, request: HttpServletRequest): String = TODO()
}
