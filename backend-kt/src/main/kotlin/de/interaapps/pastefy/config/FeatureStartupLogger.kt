package de.interaapps.pastefy.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class FeatureStartupLogger(
    private val properties: PastefyProperties,
    private val environment: Environment,
) {
    private val logger = LoggerFactory.getLogger(FeatureStartupLogger::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun logEnabledFeatures() {
        val redisHost = environment.getProperty("spring.data.redis.host", "localhost")
        val redisPort = environment.getProperty("spring.data.redis.port", "6379")
        val s3Details = "bucket=${properties.s3.bucket}, endpoint=${redactUrl(properties.s3.endpoint)}"
        val redisDetails = "host=$redisHost:$redisPort"
        val elasticDetails =
            "index=${properties.elasticsearch.indexName}, migrations=${enabled(properties.elasticsearch.migrations.enabled)}"
        val aiDetails = "provider=${blank(properties.ai.provider)}, model=${blank(properties.ai.model)}"
        val analyticsDetails =
            "table=${properties.analytics.database}.${properties.analytics.table}, migrations=${enabled(properties.analytics.migrations.enabled)}"

        logger.info(
            """
            
            Pastefy feature configuration
              Profiles: ${profiles()}
              Public API:
                public pastes: ${enabled(properties.publicPastesEnabled)}
                public stats: ${enabled(properties.publicStats)}
                list pastes: ${enabled(properties.listPastes)}
                meta tags / SEO SSR: ${enabled(properties.metaTagsEnabled)}
                login required read: ${enabled(properties.loginRequiredRead)}
                login required create: ${enabled(properties.loginRequiredCreate)}
                grant access required: ${enabled(properties.grantAccessRequired)}
              Auth:
                rate limiter: ${enabled(properties.rateLimiter.enabled)} (${properties.rateLimiter.limit} requests / ${properties.rateLimiter.windowMillis}ms)
                oauth providers: ${oauthProviders()}
              Storage and search:
                S3: ${enabled(properties.s3.enabled)}${suffix(properties.s3.enabled, s3Details)}
                Redis: ${enabled(properties.redis.enabled)}${suffix(properties.redis.enabled, redisDetails)}
                Elasticsearch: ${enabled(properties.elasticsearch.enabled)}${suffix(properties.elasticsearch.enabled, elasticDetails)}
              AI:
                paste AI: ${enabled(properties.ai.enabled)}${suffix(properties.ai.enabled, aiDetails)}
                AI job sweeper: ${enabled(properties.ai.jobs.sweeperEnabled)}
              Analytics:
                ClickHouse analytics: ${enabled(properties.analytics.enabled)}${suffix(properties.analytics.enabled, analyticsDetails)}
                track bots: ${enabled(properties.analytics.trackBots)}
                GeoIP: ${enabled(properties.analytics.geoIpMmdbPath.isNotBlank())}
              Local tooling:
                seeding: ${enabled(properties.seeding.enabled)}
            """.trimIndent(),
        )
    }

    private fun profiles(): String =
        environment.activeProfiles.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "default"

    private fun oauthProviders(): String {
        val providers = listOfNotNull(
            "interaapps".takeIf { properties.oauth.interaapps.enabled },
            "google".takeIf { properties.oauth.google.enabled },
            "github".takeIf { properties.oauth.github.enabled },
            "twitch".takeIf { properties.oauth.twitch.enabled },
            "discord".takeIf { properties.oauth.discord.enabled },
            "oidc".takeIf { properties.oauth.oidc.fullyConfigured },
        )
        return providers.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "none"
    }

    private fun enabled(value: Boolean): String = if (value) "enabled" else "disabled"

    private fun suffix(condition: Boolean, value: String): String = if (condition) " ($value)" else ""

    private fun blank(value: String): String = value.takeIf(String::isNotBlank) ?: "not configured"

    private fun redactUrl(value: String): String {
        if (value.isBlank()) return "not configured"
        return runCatching {
            val uri = java.net.URI(value)
            buildString {
                append(uri.scheme ?: "unknown")
                append("://")
                append(uri.host ?: uri.authority ?: "unknown")
                if (uri.port >= 0) append(":").append(uri.port)
            }
        }.getOrElse { "configured" }
    }
}
