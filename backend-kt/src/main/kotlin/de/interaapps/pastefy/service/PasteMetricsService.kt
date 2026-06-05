package de.interaapps.pastefy.service

import com.fasterxml.jackson.databind.ObjectMapper
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.infrastructure.analytics.AnalyticsService
import de.interaapps.pastefy.repositories.PasteCommentRepository
import de.interaapps.pastefy.repositories.PasteStarRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

data class PasteMetrics(
    val starCount: Long = 0,
    val commentCount: Long = 0,
    val viewCount: Long = 0,
)

@Service
class PasteMetricsService(
    private val pasteStarRepository: PasteStarRepository,
    private val pasteCommentRepository: PasteCommentRepository,
    private val analyticsProvider: ObjectProvider<AnalyticsService>,
    private val redisProvider: ObjectProvider<StringRedisTemplate>,
    private val objectMapper: ObjectMapper,
    private val properties: PastefyProperties,
) {
    fun getMetrics(pasteKeys: Collection<String>): Map<String, PasteMetrics> {
        val keys = pasteKeys.filter(String::isNotBlank).distinct()
        if (keys.isEmpty()) return emptyMap()

        val cached = readCached(keys)
        val missing = keys.filterNot(cached::containsKey)
        if (missing.isEmpty()) return cached

        val loaded = loadMetrics(missing)
        writeCached(loaded)
        return buildMap {
            keys.forEach { key ->
                put(key, cached[key] ?: loaded[key] ?: PasteMetrics())
            }
        }
    }

    fun getMetrics(pasteKey: String): PasteMetrics = getMetrics(listOf(pasteKey))[pasteKey] ?: PasteMetrics()

    fun invalidate(pasteKey: String) {
        if (!properties.redis.enabled) return
        runCatching {
            redisProvider.ifAvailable?.delete(cacheKey(pasteKey))
        }.onFailure {
            LOGGER.warn("Unable to evict paste metrics cache for paste {}", pasteKey, it)
        }
    }

    private fun loadMetrics(keys: List<String>): Map<String, PasteMetrics> {
        val stars = pasteStarRepository.countGroupedByPaste(keys).associate { it.paste to it.count }
        val comments = pasteCommentRepository.countGroupedByPaste(keys).associate { it.paste to it.count }
        val views = analyticsProvider.ifAvailable?.countVisitsByPaste(keys) ?: emptyMap()
        return keys.associateWith { key ->
            PasteMetrics(
                starCount = stars[key] ?: 0,
                commentCount = comments[key] ?: 0,
                viewCount = views[key] ?: 0,
            )
        }
    }

    private fun readCached(keys: List<String>): Map<String, PasteMetrics> {
        if (!properties.redis.enabled) return emptyMap()
        val redis = redisProvider.ifAvailable ?: return emptyMap()
        return runCatching {
            val cacheKeys = keys.map(::cacheKey)

            val values = redis.opsForValue().multiGet(cacheKeys) ?: emptyList()

            keys.zip(values).mapNotNull { (pasteKey, value) ->
                value?.let { pasteKey to objectMapper.readValue(it, PasteMetrics::class.java) }
            }.toMap()
        }.onFailure {
            LOGGER.warn("Unable to read paste metrics cache", it)
        }.getOrDefault(emptyMap())
    }

    private fun writeCached(metrics: Map<String, PasteMetrics>) {
        if (!properties.redis.enabled || metrics.isEmpty()) return

        val redis = redisProvider.ifAvailable ?: return
        val ttl = Duration.ofSeconds(properties.redis.metricsTtlSeconds)

        runCatching {
            metrics.forEach { (pasteKey, value) ->
                redis.opsForValue().set(cacheKey(pasteKey), objectMapper.writeValueAsString(value), ttl)
            }
        }.onFailure {
            LOGGER.warn("Unable to write paste metrics cache", it)
        }
    }

    private fun cacheKey(pasteKey: String) = "paste:$pasteKey:metrics"

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PasteMetricsService::class.java)
    }
}
