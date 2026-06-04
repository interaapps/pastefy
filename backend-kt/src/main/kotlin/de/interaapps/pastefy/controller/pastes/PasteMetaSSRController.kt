package de.interaapps.pastefy.controller.pastes

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.repositories.PasteAIInfoRepository
import de.interaapps.pastefy.repositories.PasteTagRepository
import de.interaapps.pastefy.repositories.UserRepository
import de.interaapps.pastefy.service.PasteService
import de.interaapps.pastefy.service.SeoRenderer
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class PasteMetaSSRController(
    private val pasteService: PasteService,
    private val userRepository: UserRepository,
    private val tagRepository: PasteTagRepository,
    private val aiInfoRepository: PasteAIInfoRepository,
    private val properties: PastefyProperties,
    private val seo: SeoRenderer,
) {
    @GetMapping("/{id}")
    fun getPasteMetaSSR(@PathVariable id: String): ResponseEntity<String> {
        if (!id.matches(Regex("^[A-Za-z0-9_-]{8}$"))) return ResponseEntity.notFound().build()

        val paste = pasteService.get(id) ?: return ResponseEntity.notFound().build()

        if (paste.isPrivate || paste.encrypted) return ResponseEntity.notFound().build()

        val aiInfo = paste.id?.let(aiInfoRepository::findById)?.orElse(null)

        val title =
            paste.title?.trim()?.replace(Regex("\\s+"), " ")?.takeIf(String::isNotEmpty)?.let { seo.truncate(it, 120) }
                ?: "Paste"

        val author = if (paste.isPublic) {
            paste.userId?.let(userRepository::findById)?.orElse(null)?.uniqueName?.trim()?.takeIf(String::isNotEmpty)
                ?.let { username ->
                    Author(
                        username = username,
                        displayName = paste.userId?.let(userRepository::findById)?.orElse(null)?.name?.trim()
                            ?.takeIf(String::isNotEmpty) ?: username,
                        profileUrl = seo.absoluteUrl("/@${seo.pathSegment(username)}"),
                    )
                }
        } else null

        val descriptiveTitle = title + aiInfo?.description?.takeIf(String::isNotBlank)?.let { " | $it" }.orEmpty()

        val description = when {
            title == "Paste" && author == null -> "View this paste on Pastefy."
            title == "Paste" -> "View this paste by @${author?.username} on Pastefy."
            author != null -> seo.truncate("View \"$descriptiveTitle\" by @${author.username} on Pastefy.", 180)
            else -> seo.truncate("View \"$descriptiveTitle\" on Pastefy.", 180)
        }

        val content = if (paste.isPublic) seoContent(
            pasteService.getContent(paste, withCache = false).orEmpty(),
            paste.key,
            title,
            author,
            aiInfo?.description
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

        return seo.render(page)?.let {
            ResponseEntity.ok().contentType(MediaType("text", "html", Charsets.UTF_8)).body(it)
        } ?: ResponseEntity.notFound().build()
    }

    private fun seoContent(
        content: String,
        pasteKey: String,
        title: String,
        author: Author?,
        aiDescription: String?
    ): String {
        val preview = seo.truncateWithoutEllipsis(content, properties.metaTagsPreviewLength.coerceIn(0, 16_384))

        val authorHtml = author?.let {
            "<p>By <a href=\"${seo.escapeHtml(it.profileUrl)}\">${seo.escapeHtml(it.displayName)} (@${seo.escapeHtml(it.username)})</a></p>"
        }.orEmpty()

        val tags = tagRepository.findAllByPaste(pasteKey).joinToString(" ") {
            val url = seo.absoluteUrl("/tags/${seo.pathSegment(it.tag)}")
            "<a href=\"${seo.escapeHtml(url)}\" class=\"seo-tag\">${seo.escapeHtml(it.tag)}</a>"
        }

        val aiHtml =
            aiDescription?.takeIf(String::isNotBlank)?.let { "<h2>Description</h2><p>${seo.escapeHtml(it)}</p>" }
                .orEmpty()

        return "<main id=\"seo-content\"><h1 title=\"paste-title\">${seo.escapeHtml(title)}</h1>$authorHtml<p>View and share code snippets on Pastefy.</p><pre><code>${
            seo.escapeHtml(
                preview
            )
        }</code></pre><h2>Tags</h2>$tags$aiHtml</main>"
    }

    private data class Author(val displayName: String, val username: String, val profileUrl: String)
}
