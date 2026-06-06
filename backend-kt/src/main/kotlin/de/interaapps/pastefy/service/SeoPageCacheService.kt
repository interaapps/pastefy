package de.interaapps.pastefy.service

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class SeoPageCacheService(
    private val htmlCache: SeoHtmlCacheService,
) {
    fun renderResponse(
        cacheKey: String,
        fallback: () -> ResponseEntity<String>,
        pageFactory: () -> SeoRenderer.SeoPage?,
    ): ResponseEntity<String> =
        htmlCache.render(cacheKey, pageFactory)?.let {
            ResponseEntity.ok()
                .contentType(MediaType("text", "html", Charsets.UTF_8))
                .body(it)
        } ?: fallback()
}
