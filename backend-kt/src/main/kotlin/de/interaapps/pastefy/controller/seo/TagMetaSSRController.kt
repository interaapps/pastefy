package de.interaapps.pastefy.controller.seo

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.service.FrontendIndexService
import de.interaapps.pastefy.service.SeoRenderer
import de.interaapps.pastefy.service.TagListingService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tags")
class TagMetaSSRController(
    private val properties: PastefyProperties,
    private val tags: TagListingService,
    private val seo: SeoRenderer,
    private val frontendIndex: FrontendIndexService,
) {
    @GetMapping("/{tag}")
    fun tagMeta(@PathVariable tag: String, request: HttpServletRequest): ResponseEntity<String> {
        if (!properties.publicPastesEnabled || tag.isBlank()) return frontendIndex.frontend()
        val listing = tags.getOrCreate(tag)
        val name = seo.truncate(listing.displayName?.takeIf(String::isNotBlank)?.trim() ?: listing.tag, 120)
        val description = seo.truncate(
            listing.description?.takeIf(String::isNotBlank)?.trim()
                ?: "Explore public pastes tagged \"$name\" on Pastefy.",
            180,
        )
        val page = seo.page("/tags/${seo.pathSegment(listing.tag)}", "$name | Pastefy", description)
            .content("<main id=\"seo-content\"><h1>${seo.escapeHtml(name)}</h1><p>${seo.escapeHtml(description)}</p><p>Public pastes: ${listing.pasteCount}</p></main>")
            .image(listing.imageUrl)

        return seo.render(page)
            ?.let {
                ResponseEntity
                    .ok()
                    .contentType(MediaType("text", "html", Charsets.UTF_8))
                    .body(it)
            } ?: frontendIndex.frontend()
    }
}
