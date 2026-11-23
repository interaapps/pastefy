package de.interaapps.pastefy.controller.seo

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.service.FrontendIndexService
import de.interaapps.pastefy.service.SeoPageCacheService
import de.interaapps.pastefy.service.SeoRenderer
import de.interaapps.pastefy.service.TagListingService
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
    private val seoCache: SeoPageCacheService,
    private val frontendIndex: FrontendIndexService,
) {
    @GetMapping("/{tag}")
    fun tagMeta(@PathVariable tag: String): ResponseEntity<String> {
        if (!properties.publicPastesEnabled || tag.isBlank()) return frontendIndex.frontend()

        return seoCache.renderResponse("tag:${tag.trim().lowercase()}", { frontendIndex.frontend() }) {
            val listing = tags.getOrCreate(tag)
            val name = seo.truncate(listing.displayName?.takeIf(String::isNotBlank)?.trim() ?: listing.tag, 120)
            val description = seo.truncate(
                listing.description?.takeIf(String::isNotBlank)?.trim()
                    ?: "Explore public pastes tagged \"$name\" on Pastefy.",
                180,
            )
            val details = buildMap {
                put("Public pastes", listing.pasteCount.toString())
                listing.website?.takeIf(String::isNotBlank)?.let { put("Website", it) }
                listing.icon?.takeIf(String::isNotBlank)?.let { put("Icon", it) }
            }
            seo.page("/tags/${seo.pathSegment(listing.tag)}", "$name | Pastefy", description)
                .content(
                    seo.mainContent(
                        seo.heading(1, name),
                        seo.paragraph(description),
                        seo.definitionList(details),
                    ),
                )
                .image(listing.imageUrl)
        }
    }
}
