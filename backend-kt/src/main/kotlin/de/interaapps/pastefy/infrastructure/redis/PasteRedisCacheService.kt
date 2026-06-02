package de.interaapps.pastefy.infrastructure.redis

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.entities.Paste
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
@ConditionalOnProperty(prefix = "pastefy.redis", name = ["enabled"], havingValue = "true")
class PasteRedisCacheService(
    private val redis: StringRedisTemplate,
    private val properties: PastefyProperties,
) {
    private val logger = LoggerFactory.getLogger(PasteRedisCacheService::class.java)

    fun getContent(paste: Paste): String? = runCatching {
        redis.opsForValue().get(contentKey(paste))
    }.onFailure {
        logger.warn("Unable to read Redis content cache for paste {}", paste.id, it)
    }.getOrNull()

    fun putContent(paste: Paste, content: String) {
        runCatching {
            redis.opsForValue().set(
                contentKey(paste),
                content,
                Duration.ofSeconds(properties.redis.contentTtlSeconds),
            )
        }.onFailure {
            logger.warn("Unable to populate Redis content cache for paste {}", paste.id, it)
        }
    }

    fun evictContent(paste: Paste) {
        runCatching {
            redis.delete(contentKey(paste))
        }.onFailure {
            logger.warn("Unable to evict Redis content cache for paste {}", paste.id, it)
        }
    }

    fun incrementAccessCount(paste: Paste): Long = runCatching {
        val key = accessCountKey(paste)
        val count = redis.opsForValue().increment(key) ?: 0
        if (count == 1L) {
            redis.expire(key, Duration.ofSeconds(properties.redis.accessCountTtlSeconds))
        }
        count
    }.onFailure {
        logger.warn("Unable to update Redis access count for paste {}", paste.id, it)
    }.getOrDefault(0)

    fun shouldCache(accessCount: Long): Boolean = accessCount > properties.redis.cacheAfterAccesses

    private fun contentKey(paste: Paste) = "paste:${requireNotNull(paste.id)}"

    private fun accessCountKey(paste: Paste) = "${contentKey(paste)}:accessCount"
}
