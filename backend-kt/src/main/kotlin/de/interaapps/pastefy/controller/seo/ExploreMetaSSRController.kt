package de.interaapps.pastefy.controller.seo

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.repositories.TagListingRepository
import de.interaapps.pastefy.service.FrontendIndexService
import de.interaapps.pastefy.service.SeoPageCacheService
import de.interaapps.pastefy.service.SeoPageContentService
import de.interaapps.pastefy.service.SeoPasteListService
import de.interaapps.pastefy.service.SeoRenderer
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ExploreMetaSSRController(
    private val properties: PastefyProperties,
    private val pasteLists: SeoPasteListService,
    private val tagListings: TagListingRepository,
    private val seo: SeoRenderer,
    private val seoCache: SeoPageCacheService,
    private val seoContent: SeoPageContentService,
    private val frontendIndex: FrontendIndexService,
) {
    @GetMapping("/explore")
    fun exploreMeta(): ResponseEntity<String> {
        if (!properties.publicPastesEnabled) return frontendIndex.frontend()

        return seoCache.renderResponse("explore", { frontendIndex.frontend() }) {
            val title = "Explore | Pastefy"
            val description =
                "Explore public pastes on Pastefy, discover popular tags, browse trending snippets and see the latest shared developer content."
            val topTags = tagListings.findAllByOrderByPasteCountDesc(PageRequest.of(0, 4))
            val recentTrending = pasteLists.recentTrending(6)
            val latest = pasteLists.latestPublic(6)
            val allTimeTrending = pasteLists.allTimeTrending(6)

            val tagsContent = if (topTags.isEmpty()) {
                seo.paragraph("No public tags have been listed yet.")
            } else {
                seo.unorderedList(
                    topTags.map { tag ->
                        val name = seo.normalizeText(tag.displayName, tag.tag)
                        val descriptionText = seo.normalizeText(tag.description, "Public pastes tagged \"$name\".")
                        buildString {
                            append(seo.link(seo.absoluteUrl("/tags/${seo.pathSegment(tag.tag)}"), name, "seo-tag"))
                            append(seo.paragraph(seo.truncate(descriptionText, 140)))
                            append(seo.definitionList(mapOf("Public pastes" to tag.pasteCount.toString())))
                        }
                    },
                    cssClass = "seo-tag-list",
                )
            }

            seo.page("/explore", title, description)
                .image("/thumbnail.png")
                .content(
                    seo.mainContent(
                        seo.heading(1, "Explore public pastes"),
                        seo.paragraph(description),
                        seo.section("Popular tags", tagsContent),
                        seoContent.pasteResponseListSection(
                            "Trending",
                            recentTrending,
                            "There are no trending public pastes right now.",
                        ),
                        seoContent.pasteResponseListSection(
                            "Latest public pastes",
                            latest,
                            "There are no latest public pastes yet.",
                        ),
                        seoContent.pasteResponseListSection(
                            "All-time trending",
                            allTimeTrending,
                            "There are no all-time trending public pastes yet.",
                        ),
                    ),
                )
        }
    }
}
