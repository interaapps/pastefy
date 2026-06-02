package de.interaapps.pastefy.controller.public

import de.interaapps.pastefy.auth.annotations.PublicPastesEnabled
import de.interaapps.pastefy.entities.TagListing
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/public/tags")
@PublicPastesEnabled
class TagsController {
    @GetMapping
    fun getTags(
        @RequestParam("search") search: String? = null,
    ): List<TagListing> = TODO()

    @GetMapping("/{tag}")
    fun getTag(@PathVariable tag: String): TagListing = TODO()
}
