package de.interaapps.pastefy.service

import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.repositories.PasteAIInfoRepository
import de.interaapps.pastefy.repositories.PasteTagRepository
import de.interaapps.pastefy.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class SeoPageContentService(
    private val seo: SeoRenderer,
    private val users: UserRepository,
    private val tags: PasteTagRepository,
    private val aiInfo: PasteAIInfoRepository,
    private val metrics: PasteMetricsService,
) {
    fun title(paste: Paste): String =
        seo.truncate(seo.normalizeText(paste.title, "Paste"), 120)

    fun author(paste: Paste): SeoAuthor? =
        paste.userId
            ?.let(users::findById)
            ?.orElse(null)
            ?.let { user ->
                val username = seo.normalizeText(user.uniqueName).takeIf(String::isNotBlank) ?: return@let null
                SeoAuthor(
                    displayName = seo.normalizeText(user.name, username),
                    username = username,
                    profileUrl = seo.absoluteUrl("/@${seo.pathSegment(username)}"),
                    avatar = user.avatar,
                )
            }

    fun pasteListSection(title: String, pastes: List<Paste>, emptyText: String): String {
        if (pastes.isEmpty()) return seo.section(title, seo.paragraph(emptyText))

        val metricMap = metrics.getMetrics(pastes.map { it.key })
        return seo.section(
            title,
            seo.unorderedList(
                pastes.map { paste ->
                    pasteListItem(paste, metricMap[paste.key] ?: PasteMetrics())
                },
                cssClass = "seo-paste-list",
            ),
        )
    }

    fun pasteListItem(paste: Paste, metrics: PasteMetrics): String {
        val pasteTitle = title(paste)
        val pasteUrl = seo.absoluteUrl("/${seo.pathSegment(paste.key)}")
        val author = author(paste)
        val tagLinks = tagLinks(paste, limit = 8)
        val aiDescription = paste.id
            ?.let(aiInfo::findById)
            ?.orElse(null)
            ?.description
            ?.let { seo.normalizeText(it) }
            ?.takeIf(String::isNotBlank)
            ?.let { seo.paragraph(seo.truncate(it, 180)) }
            .orEmpty()

        val metadata = buildMap {
            paste.createdAt?.let { put("Created", it.toString()) }
            author?.let { put("Author", "${it.displayName} (@${it.username})") }
            put("Views", metrics.viewCount.toString())
            put("Comments", metrics.commentCount.toString())
            put("Stars", metrics.starCount.toString())
        }

        return buildString {
            append("<article>")
            append(seo.heading(3, pasteTitle))
            append(seo.paragraph("Public paste on Pastefy."))
            append(seo.link(pasteUrl, "Open paste"))
            append(seo.definitionList(metadata))
            if (tagLinks.isNotBlank()) append("<p>$tagLinks</p>")
            append(aiDescription)
            append("</article>")
        }
    }

    fun pasteMetadata(paste: Paste, author: SeoAuthor?): String {
        val pasteMetrics = metrics.getMetrics(paste.key)
        return seo.definitionList(
            buildMap {
                paste.createdAt?.let { put("Created", it.toString()) }
                author?.let { put("Author", "${it.displayName} (@${it.username})") }
                paste.type?.let { put("Type", it.name) }
                paste.visibility?.let { put("Visibility", it.name) }
                put("Views", pasteMetrics.viewCount.toString())
                put("Comments", pasteMetrics.commentCount.toString())
                put("Stars", pasteMetrics.starCount.toString())
            },
        )
    }

    fun tagLinks(paste: Paste, limit: Int = Int.MAX_VALUE): String =
        tags.findAllByPaste(paste.key)
            .mapNotNull { it.tag.trim().takeIf(String::isNotBlank) }
            .take(limit)
            .joinToString(" ") { tag ->
                seo.link(seo.absoluteUrl("/tags/${seo.pathSegment(tag)}"), tag, "seo-tag")
            }
}

data class SeoAuthor(
    val displayName: String,
    val username: String,
    val profileUrl: String,
    val avatar: String? = null,
)
