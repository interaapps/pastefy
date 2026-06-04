package de.interaapps.pastefy.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("pastefy")
data class PastefyProperties(
    var cors: String = "",
    var loginRequiredRead: Boolean = false,
    var loginRequiredCreate: Boolean = false,
    var publicPastesEnabled: Boolean = true,
    var publicStats: Boolean = false,
    var metaTagsEnabled: Boolean = false,
    var metaTagsPreviewLength: Int = 4_096,
    var serverName: String = "http://localhost",
    var listPastes: Boolean = false,
    var paginationPageLimit: Int = 50,
    var encryptionDefault: Boolean = false,
    var customLogo: String? = null,
    var customName: String? = null,
    var customFooter: String = "",
    var customHeader: String = "",
    var customBody: String = "",
    var grantAccessRequired: Boolean = false,
    var oauthStateSecret: String = "",
    var oauth: OAuth = OAuth(),
    var rateLimiter: RateLimiter = RateLimiter(),
    var redis: Redis = Redis(),
    var s3: S3 = S3(),
    var elasticsearch: Elasticsearch = Elasticsearch(),
    var ai: AI = AI(),
    var analytics: Analytics = Analytics(),
    var seeding: Seeding = Seeding(),
) {
    data class Seeding(
        var enabled: Boolean = false,
    )

    data class RateLimiter(
        var enabled: Boolean = true,
        var windowMillis: Long = 5_000,
        var limit: Int = 5,
    )

    data class Redis(
        var enabled: Boolean = false,
        var contentTtlSeconds: Long = 1_800,
        var accessCountTtlSeconds: Long = 1_800,
        var cacheAfterAccesses: Long = 10,
    )

    data class S3(
        var enabled: Boolean = false,
        var endpoint: String = "",
        var accessKey: String = "",
        var secretKey: String = "",
        var bucket: String = "pastefy",
        var region: String? = null,
        var pasteSizeThreshold: Int = -1,
        var createBucket: Boolean = false,
    )

    data class Elasticsearch(
        var enabled: Boolean = false,
        var apiKey: String = "",
        var indexName: String = "pastefy_pastes_current",
        var indexPrefix: String = "pastefy_pastes",
        var legacyIndexName: String = "pastefy_pastes",
        var numberOfShards: Int = 1,
        var numberOfReplicas: Int = 1,
        var migrations: Migrations = Migrations(),
    ) {
        data class Migrations(
            var enabled: Boolean = true,
        )
    }

    data class AI(
        var enabled: Boolean = false,
        var provider: String = "",
        var model: String = "",
        var engagementThreshold: Int = 100,
        var jobs: Jobs = Jobs(),
    ) {
        data class Jobs(
            var workers: Int = 2,
            var pollIntervalMillis: Long = 5_000,
            var leaseSeconds: Long = 120,
            var maxAttempts: Int = 3,
            var retryDelaySeconds: Long = 60,
            var sweeperEnabled: Boolean = false,
            var sweepIntervalMillis: Long = 300_000,
        )
    }

    data class Analytics(
        var enabled: Boolean = false,
        var jdbcUrl: String = "",
        var database: String = "default",
        var table: String = "pastefy_analytics_visits",
        var user: String = "default",
        var password: String = "",
        var retentionDays: Int = 90,
        var batchSize: Int = 1_000,
        var queueCapacity: Int = 100_000,
        var flushIntervalMillis: Long = 1_000,
        var httpConnectTimeoutMillis: Long = 2_000,
        var httpRequestTimeoutMillis: Long = 5_000,
        var ipHashSalt: String = "",
        var ipSource: String = "direct",
        var ipHeader: String = "",
        var geoIpMmdbPath: String = "",
        var trackBots: Boolean = true,
        var migrations: Migrations = Migrations(),
    ) {
        data class Migrations(
            var enabled: Boolean = false,
            var repairFailedHistory: Boolean = false,
        )
    }

    data class OAuth(
        var callbackBaseUrl: String = "http://localhost:8080",
        var interaapps: Client = Client(),
        var google: Client = Client(),
        var github: Client = Client(),
        var twitch: Client = Client(),
        var discord: Client = Client(),
        var oidc: OidcClient = OidcClient(),
    )

    open class Client(
        var clientId: String = "",
        var clientSecret: String = "",
        var scopes: List<String> = emptyList(),
    ) {
        val enabled: Boolean get() = clientId.isNotBlank() && clientSecret.isNotBlank()
    }

    class OidcClient(
        clientId: String = "",
        clientSecret: String = "",
        scopes: List<String> = emptyList(),
        var authorizationEndpoint: String = "",
        var tokenEndpoint: String = "",
        var userInfoEndpoint: String = "",
    ) : Client(clientId, clientSecret, scopes) {
        val fullyConfigured: Boolean
            get() = enabled && authorizationEndpoint.isNotBlank() && tokenEndpoint.isNotBlank() && userInfoEndpoint.isNotBlank()
    }
}
