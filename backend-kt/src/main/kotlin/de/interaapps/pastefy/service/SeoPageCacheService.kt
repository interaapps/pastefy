package de.interaapps.pastefy.service

import org.springframework.cache.CacheManager
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class SeoPageCacheService(
    private val cacheManager: CacheManager,
    private val seo: SeoRenderer,
) {
    fun renderResponse(
        cacheKey: String,
        page: SeoRenderer.SeoPage?,
        fallback: () -> ResponseEntity<String>,
    ): ResponseEntity<String> =
        render(cacheKey, page)?.let {
            ResponseEntity.ok()
                .contentType(MediaType("text", "html", Charsets.UTF_8))
                .body(it)
        } ?: fallback()

    private fun render(cacheKey: String, page: SeoRenderer.SeoPage?): String? {
        if (page == null) return null

        val cache = cacheManager.getCache(SEO_CACHE_NAME)
        cache?.get(cacheKey, String::class.java)?.let { return it }

        val html = seo.render(page) ?: return null
        if (html.contains("""id="seo-content"""")) {
            cache?.put(cacheKey, html)
        }
        return html
    }

    companion object {
        private const val SEO_CACHE_NAME = "seo-pages"
    }
}
