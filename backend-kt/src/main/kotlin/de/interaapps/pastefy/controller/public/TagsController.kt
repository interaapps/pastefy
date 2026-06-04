package de.interaapps.pastefy.controller.public

import de.interaapps.pastefy.auth.annotations.PublicPastesEnabled
import de.interaapps.pastefy.entities.TagListing
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import de.interaapps.pastefy.repositories.TagListingRepository
import de.interaapps.pastefy.service.TagListingService
import org.springframework.data.domain.PageRequest

@RestController
@RequestMapping("/api/v2/public/tags")
@PublicPastesEnabled
class TagsController(
    private val repository: TagListingRepository,
    private val tags: TagListingService,
) {
    @GetMapping
    fun getTags(
        @RequestParam("search") search: String? = null,
        @RequestParam("page", defaultValue = "1") page: Int,
        @RequestParam("page_limit", defaultValue = "10") pageLimit: Int,
    ): List<TagListing> {
        val pageable = PageRequest.of(page.coerceAtLeast(1) - 1, pageLimit.coerceIn(1, 100))
        return search?.trim()?.takeIf(String::isNotEmpty)?.let {
            repository.findAllByTagContainingIgnoreCaseOrDisplayNameContainingIgnoreCaseOrderByPasteCountDesc(
                it,
                it,
                pageable
            )
        } ?: repository.findAllByOrderByPasteCountDesc(pageable)
    }

    @GetMapping("/{tag}")
    fun getTag(@PathVariable tag: String): TagListing = tags.getOrCreate(tag)
}
