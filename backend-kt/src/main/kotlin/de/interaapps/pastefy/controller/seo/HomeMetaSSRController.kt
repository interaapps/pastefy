package de.interaapps.pastefy.controller.seo

import de.interaapps.pastefy.service.FrontendIndexService
import de.interaapps.pastefy.service.SeoPageCacheService
import de.interaapps.pastefy.service.SeoRenderer
import de.interaapps.pastefy.service.StatsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HomeMetaSSRController(
    private val seo: SeoRenderer,
    private val seoCache: SeoPageCacheService,
    private val stats: StatsService,
    private val frontendIndex: FrontendIndexService,
) {
    @GetMapping(value = ["/", "/home"])
    fun homeMeta(): ResponseEntity<String> {
        val appStats = runCatching { stats.get() }.getOrNull()
        val title = "Pastefy • Share your code snippets"
        val description =
            "Pastefy is an open source pastebin and GitHub Gist alternative for sharing, organizing, previewing and protecting code snippets."

        val highlights = listOf(
            seo.link(seo.absoluteUrl("/"), "Share code snippets with syntax highlighting and raw access."),
            seo.link(seo.absoluteUrl("/"), "Organize pastes in folders for web, mobile, scripts and other projects."),
            seo.link(seo.absoluteUrl("/"), "Keep sensitive snippets private or encrypted when they should not be public."),
            seo.link(seo.absoluteUrl("/tools"), "Preview, convert and inspect developer files with Pastefy tools."),
            seo.link("https://docs.pastefy.app/api/", "Use the Pastefy API and client integrations."),
        )

        val statDetails = appStats?.let {
            seo.definitionList(
                mapOf(
                    "Created pastes" to it.createdPastes.toString(),
                    "Users" to it.userCount.toString(),
                    "Tags" to it.tagCount.toString(),
                    "Folders" to it.folderCount.toString(),
                ),
            )
        }.orEmpty()

        val page = seo.page("/", title, description)
            .image("/thumbnail.png")
            .content(
                seo.mainContent(
                    seo.heading(1, "Pastefy"),
                    seo.paragraph(description),
                    seo.section("What you can do", seo.unorderedList(highlights)),
                    seo.section("Platform", statDetails),
                    seo.section(
                        "Explore",
                        seo.paragraph("Browse public pastes, discover tags and search shared snippets from the Pastefy community."),
                        seo.link(seo.absoluteUrl("/explore"), "Explore public pastes"),
                    ),
                ),
            )

        return seoCache.renderResponse("home", page) { frontendIndex.frontend() }
    }
}
