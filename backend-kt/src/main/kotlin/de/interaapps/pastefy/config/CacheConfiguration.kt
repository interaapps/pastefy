package de.interaapps.pastefy.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.ObjectProvider
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import java.time.Duration

@Configuration
class CacheConfiguration(
    private val properties: PastefyProperties,
) {
    @Bean
    fun cacheManager(
        redisConnectionFactoryProvider: ObjectProvider<RedisConnectionFactory>,
    ): CacheManager {
        if (properties.redis.enabled) {
            redisConnectionFactoryProvider.ifAvailable?.let { connectionFactory ->
                return redisCacheManager(connectionFactory)
            }
        }

        return caffeineCacheManager()
    }

    private fun redisCacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val cacheProperties = properties.cache
        val defaultConfiguration = redisConfiguration(cacheProperties.defaultTtlSeconds)
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfiguration)
            .withInitialCacheConfigurations(
                mapOf(
                    "app-info" to redisConfiguration(cacheProperties.defaultTtlSeconds),
                    "public-tag" to redisConfiguration(cacheProperties.defaultTtlSeconds),
                    "seo-pages" to redisConfiguration(cacheProperties.seoTtlSeconds),
                ),
            )
            .disableCreateOnMissingCache()
            .transactionAware()
            .build()
    }

    private fun redisConfiguration(
        ttlSeconds: Long,
    ): RedisCacheConfiguration =
        RedisCacheConfiguration.defaultCacheConfig()
            .disableCachingNullValues()
            .entryTtl(Duration.ofSeconds(ttlSeconds.coerceAtLeast(1)))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()),
            )
            .prefixCacheNameWith("pastefy:cache:")

    private fun caffeineCacheManager(): CacheManager {
        val cacheProperties = properties.cache
        return SimpleCacheManager().apply {
            setCaches(
                listOf(
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
