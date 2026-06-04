package de.interaapps.pastefy.service

import de.interaapps.pastefy.config.PastefyProperties
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.web.util.HtmlUtils
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class SeoRenderer(
    private val properties: PastefyProperties,
) {
    private val html = loadHtml()

    fun render(page: SeoPage?): String? {
        if (!properties.metaTagsEnabled || page == null) return null
        val template = html ?: return null
        return template
            .replace(TITLE_REPLACEMENT, escapeHtml(page.title))
            .replace(META_REPLACEMENT, tagsToHtml(page))
            .replace(SEO_CONTENT_REPLACEMENT, page.content)
    }

    fun page(canonicalPath: String, title: String, description: String) =
        SeoPage(absoluteUrl(canonicalPath), title, description)

    fun pathSegment(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")

    fun truncate(value: String, maxLength: Int): String =
        if (value.codePointCount(0, value.length) <= maxLength) value
        else value.substring(0, value.offsetByCodePoints(0, maxLength - 3)) + "..."

    fun truncateWithoutEllipsis(value: String, maxLength: Int): String =
        if (value.codePointCount(0, value.length) <= maxLength) value
        else value.substring(0, value.offsetByCodePoints(0, maxLength))

    fun escapeHtml(value: String?) = HtmlUtils.htmlEscape(value.orEmpty())

    private fun loadHtml(): String? = runCatching {
        prepareHtml(ClassPathResource("static/index.html").inputStream.bufferedReader().use { it.readText() })
    }.onFailure {
        LOGGER.warn("Unable to prepare static/index.html for SEO metadata", it)
    }.getOrNull()

    private fun prepareHtml(source: String): String {
        val start = source.indexOf(META_START_TAG)
        val end = source.indexOf(META_END_TAG, start + META_START_TAG.length)
        require(start >= 0 && end >= 0) { "SEO metadata placeholders are missing in static/index.html" }
        var prepared = source.substring(0, start) + META_REPLACEMENT + source.substring(end + META_END_TAG.length)
        prepared = APP_MOUNT.replace(prepared) { result -> result.groupValues[1] + SEO_CONTENT_REPLACEMENT + "</div>" }
        require(SEO_CONTENT_REPLACEMENT in prepared) { "Vue app mount is missing in static/index.html" }
        return if (TITLE.containsMatchIn(prepared)) {
            TITLE.replaceFirst(prepared, "<title>$TITLE_REPLACEMENT</title>")
        } else {
            prepared.replace("</head>", "<title>$TITLE_REPLACEMENT</title></head>")
        }
    }

    private fun tagsToHtml(page: SeoPage): String = buildString {
        page.seoTags.forEach { (name, value) -> appendMeta("name", name, value) }
        append("<link rel=\"canonical\" href=\"${escapeHtml(page.canonicalUrl)}\" />")
        page.openGraphTags.forEach { (name, value) -> appendMeta("property", name, value) }
        page.twitterTags.forEach { (name, value) -> appendMeta("name", name, value) }
    }

    private fun StringBuilder.appendMeta(attribute: String, name: String, value: String) {
        append("<meta $attribute=\"${escapeHtml(name)}\" content=\"${escapeHtml(value)}\" />")
    }

    fun absoluteUrl(pathOrUrl: String): String {
        if (pathOrUrl.startsWith("http://", true) || pathOrUrl.startsWith("https://", true)) return pathOrUrl
        val configured = properties.serverName.trim().ifBlank { "http://localhost" }
        val base = if (configured.startsWith("http://", true) || configured.startsWith("https://", true)) configured else "https://$configured"
        return base.trimEnd('/') + "/" + pathOrUrl.trimStart('/')
    }

    inner class SeoPage(
        val canonicalUrl: String,
        val title: String,
        description: String,
    ) {
        val seoTags = linkedMapOf("description" to description)
        val openGraphTags = linkedMapOf(
            "og:site_name" to "Pastefy",
            "og:type" to "website",
            "og:title" to title,
            "og:description" to description,
            "og:url" to canonicalUrl,
        )
        val twitterTags = linkedMapOf(
            "twitter:card" to "summary",
            "twitter:title" to title,
            "twitter:description" to description,
            "twitter:url" to canonicalUrl,
        )
        var content: String = ""
            private set

        fun content(value: String) = apply { content = value }

        fun meta(name: String, value: String?) = apply { value?.let { seoTags[name] = it } }

        fun openGraph(name: String, value: String?) = apply { value?.let { openGraphTags[name] = it } }

        fun twitter(name: String, value: String?) = apply { value?.let { twitterTags[name] = it } }

        fun type(value: String) = openGraph("og:type", value)

        fun image(value: String?) = apply {
            value?.trim()?.takeIf(String::isNotEmpty)?.let {
                val url = absoluteUrl(it)
                openGraphTags["og:image"] = url
                twitterTags["twitter:card"] = "summary_large_image"
                twitterTags["twitter:image"] = url
            }
        }
    }

    companion object {
        private const val META_START_TAG = "<!--META_TAGS-->"
        private const val META_END_TAG = "<!--META_TAGS_END-->"
        private const val META_REPLACEMENT = "<!--META_TAGS_REPLACEMENT-->"
        private const val TITLE_REPLACEMENT = "<!--TITLE_REPLACEMENT-->"
        private const val SEO_CONTENT_REPLACEMENT = "<!--SEO_CONTENT_REPLACEMENT-->"
        private val TITLE = Regex("(?is)<title\\b[^>]*>.*?</title>")
        private val APP_MOUNT = Regex("(?is)(<div\\s+id=[\"']app[\"'][^>]*>)\\s*</div>")
        private val LOGGER = LoggerFactory.getLogger(SeoRenderer::class.java)
    }
}
