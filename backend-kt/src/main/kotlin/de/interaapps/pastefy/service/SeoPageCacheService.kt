package de.interaapps.pastefy.service

import de.interaapps.pastefy.config.PastefyProperties
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ExecutionException
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

@Service
class SeoPageCacheService(
    private val htmlCache: SeoHtmlCacheService,
    private val cacheManager: CacheManager,
    private val properties: PastefyProperties,
) {
    private val threadCounter = AtomicInteger()
    private val executor = ThreadPoolExecutor(
        properties.seo.executorThreads.coerceAtLeast(1),
        properties.seo.executorThreads.coerceAtLeast(1),
        0L,
        TimeUnit.MILLISECONDS,
        ArrayBlockingQueue(properties.seo.executorQueueCapacity.coerceAtLeast(1)),
        { runnable ->
            Thread(runnable, "pastefy-seo-render-${threadCounter.incrementAndGet()}").apply {
                isDaemon = true
            }
        },
        ThreadPoolExecutor.AbortPolicy(),
    )

    fun renderResponse(
        cacheKey: String,
        fallback: () -> ResponseEntity<String>,
        pageFactory: () -> SeoRenderer.SeoPage?,
    ): ResponseEntity<String> =
        renderWithBudget(cacheKey, pageFactory)?.let {
            ResponseEntity.ok()
                .contentType(MediaType("text", "html", Charsets.UTF_8))
                .body(it)
        } ?: fallback()

    private fun renderWithBudget(cacheKey: String, pageFactory: () -> SeoRenderer.SeoPage?): String? {
        cachedHtml(cacheKey)?.let { return it }

        if (!properties.seo.renderTimeoutEnabled || properties.seo.renderTimeoutMillis <= 0) {
            return renderSafely(cacheKey, pageFactory)
        }

        val future = try {
            executor.submit<String?> { renderSafely(cacheKey, pageFactory) }
        } catch (exception: RejectedExecutionException) {
            LOGGER.debug("SEO render queue is full for {}; falling back to frontend", cacheKey, exception)
            return null
        }

        return try {
            future.get(properties.seo.renderTimeoutMillis.coerceAtLeast(1), TimeUnit.MILLISECONDS)
        } catch (exception: TimeoutException) {
            future.cancel(true)
            LOGGER.debug(
                "SEO render timed out after {}ms for {}; falling back to frontend",
                properties.seo.renderTimeoutMillis,
                cacheKey,
            )
            null
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            future.cancel(true)
            null
        } catch (exception: ExecutionException) {
            LOGGER.warn("SEO render failed for {}; falling back to frontend", cacheKey, exception.cause ?: exception)
            null
        }
    }

    private fun cachedHtml(cacheKey: String): String? =
        runCatching {
            cacheManager.getCache(SEO_PAGES_CACHE)?.get(cacheKey, String::class.java)
        }.getOrNull()

    private fun renderSafely(cacheKey: String, pageFactory: () -> SeoRenderer.SeoPage?): String? =
        runCatching {
            htmlCache.render(cacheKey, pageFactory)
        }.onFailure {
            LOGGER.warn("SEO render failed for {}; falling back to frontend", cacheKey, it)
        }.getOrNull()

    @PreDestroy
    fun shutdown() {
        executor.shutdownNow()
    }

    companion object {
        private const val SEO_PAGES_CACHE = "seo-pages"
        private val LOGGER = LoggerFactory.getLogger(SeoPageCacheService::class.java)
    }
}
