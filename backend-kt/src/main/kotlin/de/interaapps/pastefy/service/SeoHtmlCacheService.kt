package de.interaapps.pastefy.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class SeoHtmlCacheService(
    private val seo: SeoRenderer,
) {
    @Cacheable(cacheNames = ["seo-pages"], key = "#p0", unless = "#result == null")
    fun render(cacheKey: String, pageFactory: () -> SeoRenderer.SeoPage?): String? {
        val page = pageFactory() ?: return null
        val html = seo.render(page) ?: return null
        return html.takeIf { it.contains("""id="seo-content"""") }
    }
}
