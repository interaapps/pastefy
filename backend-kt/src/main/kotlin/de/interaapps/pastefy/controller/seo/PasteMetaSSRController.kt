package de.interaapps.pastefy.controller.seo

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.repositories.PasteAIInfoRepository
import de.interaapps.pastefy.service.FrontendIndexService
import de.interaapps.pastefy.service.PasteService
import de.interaapps.pastefy.service.SeoAuthor
import de.interaapps.pastefy.service.SeoPageCacheService
import de.interaapps.pastefy.service.SeoPageContentService
import de.interaapps.pastefy.service.SeoRenderer
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PasteMetaSSRController(
    private val pasteService: PasteService,
    private val aiInfoRepository: PasteAIInfoRepository,
    private val properties: PastefyProperties,
    private val seo: SeoRenderer,
    private val seoCache: SeoPageCacheService,
    private val seoContent: SeoPageContentService,
    private val frontendIndex: FrontendIndexService,
) {
    @GetMapping("/{id}")
    fun getPasteMetaSSR(@PathVariable id: String): ResponseEntity<String> {
        if (!id.matches(Regex("^[A-Za-z0-9_-]{8}$"))) return frontendIndex.frontend()

        return seoCache.renderResponse("paste:$id", { frontendIndex.frontend() }) {
            val paste = pasteService.get(id) ?: return@renderResponse null

            if (paste.isPrivate || paste.encrypted) return@renderResponse null

            val aiInfo = paste.id?.let(aiInfoRepository::findById)?.orElse(null)

            val title = seoContent.title(paste)
            val author = if (paste.isPublic) seoContent.author(paste) else null

            val descriptiveTitle = title + aiInfo?.description?.takeIf(String::isNotBlank)?.let { " | $it" }.orEmpty()

            val description = when {
                title == "Paste" && author == null -> "View this paste on Pastefy."
                title == "Paste" -> "View this paste by @${author?.username} on Pastefy."
                author != null -> seo.truncate("View \"$descriptiveTitle\" by @${author.username} on Pastefy.", 180)
                else -> seo.truncate("View \"$descriptiveTitle\" on Pastefy.", 180)
            }

            val content = if (paste.isPublic) pasteSeoContent(
                pasteService.getContent(paste, withCache = false).orEmpty(),
                paste,
                title,
                author,
                aiInfo?.description,
            ) else ""

            val page = seo.page("/$id", title, description)
                .type("article")
                .image("/$id/thumbnail.png")
                .content(content)

            author?.let {
                page.meta("author", "${it.displayName} (@${it.username})")
                    .openGraph("article:author", it.profileUrl)
                    .twitter("twitter:creator", "@${it.username}")
            }

            page
        }
    }

    private fun pasteSeoContent(
        content: String,
        paste: Paste,
        title: String,
        author: SeoAuthor?,
        aiDescription: String?
    ): String {
        val preview = seo.truncateWithoutEllipsis(content, properties.metaTagsPreviewLength.coerceIn(0, 16_384))

        val authorHtml = author?.let {
            "<p>By <a href=\"${seo.escapeHtml(it.profileUrl)}\">${seo.escapeHtml(it.displayName)} (@${seo.escapeHtml(it.username)})</a></p>"
        }.orEmpty()

        val aiHtml =
            aiDescription?.takeIf(String::isNotBlank)?.let { "<h2>Description</h2><p>${seo.escapeHtml(it)}</p>" }
                .orEmpty()
        val tags = seoContent.tagLinks(paste)

        return seo.mainContent(
            seo.heading(1, title, "title=\"paste-title\""),
            authorHtml,
            seo.paragraph("View and share code snippets on Pastefy."),
            "<pre><code>${seo.escapeHtml(preview)}</code></pre>",
            seo.section("Paste details", seoContent.pasteMetadata(paste, author)),
            seo.section("Tags", tags),
            aiHtml,
        )
    }
}
