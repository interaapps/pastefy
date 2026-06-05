package de.interaapps.pastefy.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.ObjectProvider
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.CompositeCacheManager
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class CacheConfiguration(
    private val properties: PastefyProperties,
) {
    @Bean
    fun cacheManager(
        redisConnectionFactoryProvider: ObjectProvider<RedisConnectionFactory>,
    ): CacheManager {
        val caffeine = caffeineCacheManager()
        if (properties.redis.enabled) {
            redisConnectionFactoryProvider.ifAvailable?.let { connectionFactory ->
                return CompositeCacheManager(redisSeoCacheManager(connectionFactory), caffeine)
            }
        }

        return caffeine
    }

    private fun redisSeoCacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val cacheProperties = properties.cache
        val configuration = redisSeoConfiguration(cacheProperties.seoTtlSeconds)
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(configuration)
            .withInitialCacheConfigurations(
                mapOf(
                    "seo-pages" to configuration,
                ),
            )
            .disableCreateOnMissingCache()
            .transactionAware()
            .build()
    }

    private fun redisSeoConfiguration(
        ttlSeconds: Long,
    ): RedisCacheConfiguration =
        RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .entryTtl(Duration.ofSeconds(ttlSeconds.coerceAtLeast(1)))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer.UTF_8))
            .prefixCacheNameWith("pastefy:cache:")

    private fun caffeineCacheManager(): CacheManager {
        val cacheProperties = properties.cache
        return SimpleCacheManager().apply {
            setCaches(
                listOf(
                    caffeineCache("frontend-index", cacheProperties.defaultTtlSeconds, 16),
                    caffeineCache("app-info", cacheProperties.defaultTtlSeconds, 64),
                    caffeineCache("public-tag", cacheProperties.defaultTtlSeconds, cacheProperties.maxSize),
                    caffeineCache("seo-pages", cacheProperties.seoTtlSeconds, cacheProperties.maxSize),
                ),
            )
        }
    }

    private fun caffeineCache(name: String, ttlSeconds: Long, maximumSize: Long): CaffeineCache =
        CaffeineCache(
            name,
            Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(ttlSeconds.coerceAtLeast(1)))
                .maximumSize(maximumSize.coerceAtLeast(1))
                .build(),
            false,
        )
}
