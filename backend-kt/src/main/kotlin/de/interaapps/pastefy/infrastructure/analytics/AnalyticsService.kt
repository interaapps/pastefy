package de.interaapps.pastefy.infrastructure.analytics

import com.maxmind.db.CHMCache
import com.maxmind.geoip2.DatabaseReader
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.dto.analytics.*
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteVisibility
import jakarta.annotation.PreDestroy
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.math.BigInteger
import java.net.InetAddress
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.sql.Timestamp
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong

@Service
@ConditionalOnProperty(prefix = "pastefy.analytics", name = ["enabled"], havingValue = "true")
class AnalyticsService(
    private val jdbc: JdbcTemplate,
    private val properties: PastefyProperties,
) {
    private val config = properties.analytics
    private val table = "${identifier(config.database)}.${identifier(config.table)}"
    private val queue =
        ArrayBlockingQueue<VisitEvent>(config.queueCapacity.coerceAtLeast(config.batchSize.coerceAtLeast(1)))
    private val droppedEvents = AtomicLong()
    private val writerFailures = AtomicLong()
    private val geoIpReader = createGeoIpReader(config.geoIpMmdbPath)

    enum class VisitType { PAGE, RAW }

    fun track(request: HttpServletRequest, paste: Paste, visitor: User?, visitType: VisitType) {
        val userAgent = UserAgentInfo.parse(request.getHeader("User-Agent"))
        if (!config.trackBots && userAgent.bot) return
        val ip = resolveIp(request)
        val geo = lookupGeo(ip)
        val refererHost = refererHost(request.getHeader("Referer"))
        val event = VisitEvent(
            pasteKey = paste.key,
            pasteVisibility = paste.visibility?.name ?: PasteVisibility.UNLISTED.name,
            pasteUserId = paste.userId,
            visitType = visitType.name,
            visitedAt = Instant.now(),
            country = geo.country,
            region = geo.region,
            city = geo.city,
            visitorUserId = visitor?.id,
            browser = userAgent.browser,
            deviceType = userAgent.deviceType,
            os = userAgent.os,
            ipHash = hashIp(ip),
            refererHost = refererHost,
            acquisition = acquisition(refererHost, request.getHeader("Host")),
            isBot = userAgent.bot,
        )
        if (!queue.offer(event)) logDroppedEvent()
    }

    fun query(query: AnalyticsQuery): AnalyticsResponse {
        if (!config.trackBots) {
            query.filters.remove("is_bot")
            if (query.groupBy == "is_bot") query.groupBy = "country"
        }
        require(query.groupBy in AnalyticsQuery.FILTERS) { "Unsupported analytics group field" }
        val where = buildWhere(query)
        val response = AnalyticsResponse(botTrackingEnabled = config.trackBots)
        if (query.includeSummary) {
            val botVisits = if (config.trackBots) ", countIf(is_bot = 1) AS bot_visits" else ""
            jdbc.queryForList("SELECT count() AS total_visits, uniqExact(ip_hash) AS unique_visitors$botVisits FROM $table$where")
                .firstOrNull()?.let {
                    response.totalVisits = (it["total_visits"] as Number).toLong()
                    response.uniqueVisitors = (it["unique_visitors"] as Number).toLong()
                    if (config.trackBots) response.botVisits = (it["bot_visits"] as Number).toLong()
                }
            val bucket = when (query.interval) {
                "hour" -> "toStartOfHour(visited_at)"
                "week" -> "toStartOfWeek(visited_at)"
                "month" -> "toStartOfMonth(visited_at)"
                else -> "toStartOfDay(visited_at)"
            }
            jdbc.queryForList(
                "SELECT formatDateTime($bucket, '%FT%TZ', 'UTC') AS bucket, count() AS visits, " +
                        "uniqExact(ip_hash) AS unique_visitors FROM $table$where GROUP BY bucket ORDER BY bucket",
            ).forEach {
                response.series += SeriesPoint(
                    it["bucket"].toString(),
                    (it["visits"] as Number).toLong(),
                    (it["unique_visitors"] as Number).toLong()
                )
            }
        }
        if (query.includeBreakdown) {
            jdbc.queryForList(
                "SELECT toString(${query.groupBy}) AS value, count() AS visits, uniqExact(ip_hash) AS unique_visitors " +
                        "FROM $table$where GROUP BY value ORDER BY visits DESC LIMIT 25",
            ).forEach {
                response.breakdown += BreakdownPoint(
                    it["value"].toString(),
                    (it["visits"] as Number).toLong(),
                    (it["unique_visitors"] as Number).toLong()
                )
            }
        }
        return response
    }

    @Scheduled(fixedDelayString = "\${pastefy.analytics.flush-interval-millis:1000}")
    fun flush() {
        do {
            val batch = mutableListOf<VisitEvent>()
            queue.drainTo(batch, config.batchSize.coerceAtLeast(1))
            if (batch.isEmpty()) return
            try {
                jdbc.batchUpdate(
                    "INSERT INTO $table (paste_key, paste_visibility, paste_user_id, visit_type, visited_at, country, region, city, " +
                            "visitor_user_id, browser, device_type, os, ip_hash, referer_host, acquisition, is_bot) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    batch,
                    batch.size,
                ) { statement, event ->
                    statement.setString(1, event.pasteKey)
                    statement.setString(2, event.pasteVisibility)
                    statement.setString(3, event.pasteUserId)
                    statement.setString(4, event.visitType)
                    statement.setTimestamp(5, Timestamp.from(event.visitedAt))
                    statement.setString(6, event.country)
                    statement.setString(7, event.region)
                    statement.setString(8, event.city)
                    statement.setString(9, event.visitorUserId)
                    statement.setString(10, event.browser)
                    statement.setString(11, event.deviceType)
                    statement.setString(12, event.os)
                    statement.setObject(13, event.ipHash)
                    statement.setString(14, event.refererHost)
                    statement.setString(15, event.acquisition)
                    statement.setInt(16, if (event.isBot) 1 else 0)
                }
                writerFailures.set(0)
            } catch (exception: RuntimeException) {
                val failures = writerFailures.incrementAndGet()
                if (failures == 1L || failures % 60L == 0L) LOGGER.warn(
                    "Could not write analytics batch ({} consecutive failures)",
                    failures,
                    exception
                )
                batch.forEach { if (!queue.offer(it)) logDroppedEvent() }
                return
            }
        } while (queue.size >= config.batchSize)
    }

    @PreDestroy
    fun close() {
        flush()
        geoIpReader?.close()
    }

    private fun buildWhere(query: AnalyticsQuery): String {
        val clauses = mutableListOf(
            "visited_at >= parseDateTime64BestEffort('${DATE_TIME.format(query.from)}')",
            "visited_at <= parseDateTime64BestEffort('${DATE_TIME.format(query.to)}')",
        )
        if (!config.trackBots) clauses += "is_bot = 0"
        query.filters.forEach { (field, value) ->
            require(field in AnalyticsQuery.FILTERS) { "Unsupported analytics filter" }
            clauses += if (field == "is_bot") "is_bot = ${if (value == "1" || value.toBoolean()) 1 else 0}" else "$field = '${
                escape(
                    value
                )
            }'"
        }
        return " WHERE ${clauses.joinToString(" AND ")}"
    }

    private fun resolveIp(request: HttpServletRequest): String {
        val value = when {
            config.ipHeader.isNotBlank() -> request.getHeader(config.ipHeader)
            config.ipSource.lowercase() in setOf("x-forwarded-for", "xff") -> request.getHeader("X-Forwarded-For")
            config.ipSource.lowercase() in setOf(
                "cloudflare",
                "cf-connecting-ip"
            ) -> request.getHeader("CF-Connecting-IP")

            else -> request.remoteAddr
        }?.substringBefore(',')?.trim().orEmpty()
        if (value.startsWith("[") && value.contains(']')) return value.substring(1, value.indexOf(']'))
        return if (value.count { it == ':' } == 1 && value.contains('.')) value.substringBefore(':') else value
    }

    private fun hashIp(ip: String): BigInteger? = ip.takeIf(String::isNotBlank)?.let {
        BigInteger(
            1,
            MessageDigest.getInstance("SHA-256").digest("${config.ipHashSalt}:$it".toByteArray(StandardCharsets.UTF_8))
                .copyOf(8)
        )
    }

    private fun lookupGeo(ip: String): GeoLocation {
        val reader = geoIpReader ?: return GeoLocation()
        if (ip.isBlank()) return GeoLocation()
        return runCatching {
            val city = reader.tryCity(InetAddress.getByName(ip)).orElse(null) ?: return GeoLocation()
            GeoLocation(
                city.country.isoCode.orEmpty(),
                city.mostSpecificSubdivision.isoCode.orEmpty(),
                city.city.name.orEmpty()
            )
        }.getOrDefault(GeoLocation())
    }

    private fun createGeoIpReader(path: String): DatabaseReader? =
        path.takeIf(String::isNotBlank)?.let {
            runCatching { DatabaseReader.Builder(File(it)).withCache(CHMCache()).build() }
                .onFailure { error ->
                    LOGGER.warn(
                        "GeoIP database could not be loaded; location fields stay empty",
                        error
                    )
                }
                .getOrNull()
        }

    private fun logDroppedEvent() {
        val dropped = droppedEvents.incrementAndGet()
        if (dropped == 1L || dropped % 1_000L == 0L) LOGGER.warn(
            "Analytics queue is full; dropped {} visit event(s)",
            dropped
        )
    }

    private fun refererHost(value: String?): String = runCatching {
        URI.create(value.orEmpty()).host?.lowercase()?.takeIf { it.length <= 253 }.orEmpty()
    }.getOrDefault("")

    private fun acquisition(referer: String, requestHost: String?): String {
        if (referer.isBlank()) return "DIRECT"
        if (referer == requestHost.orEmpty().substringBefore(':').lowercase()) return "INTERNAL"
        if (referer.containsAny(
                "google.",
                "bing.",
                "duckduckgo.",
                "yahoo.",
                "ecosia.",
                "brave."
            )
        ) return "ORGANIC_SEARCH"
        if (referer.containsAny("github.com", "gitlab.com")) return "DEVELOPER_REFERRAL"
        if (referer.containsAny(
                "twitter.com",
                "x.com",
                "facebook.com",
                "linkedin.com",
                "reddit.com",
                "mastodon.",
                "bsky.app"
            )
        ) return "SOCIAL"
        return "REFERRAL"
    }

    private fun identifier(value: String): String = value.takeIf { it.matches(Regex("[A-Za-z_][A-Za-z0-9_]*")) }
        ?: throw IllegalArgumentException("Invalid ClickHouse identifier: $value")

    private fun escape(value: String) = value.replace("\\", "\\\\").replace("'", "\\'")

    private data class VisitEvent(
        val pasteKey: String, val pasteVisibility: String, val pasteUserId: String?, val visitType: String,
        val visitedAt: Instant, val country: String, val region: String, val city: String, val visitorUserId: String?,
        val browser: String, val deviceType: String, val os: String, val ipHash: BigInteger?, val refererHost: String,
        val acquisition: String, val isBot: Boolean,
    )

    private data class GeoLocation(val country: String = "", val region: String = "", val city: String = "")
    private data class UserAgentInfo(val browser: String, val deviceType: String, val os: String, val bot: Boolean) {
        companion object {
            fun parse(raw: String?): UserAgentInfo {
                val ua = raw.orEmpty().lowercase()
                val bot = ua.containsAny(
                    "bot",
                    "crawler",
                    "spider",
                    "slurp",
                    "preview",
                    "wget",
                    "curl/",
                    "httpclient",
                    "python-requests",
                    "uptime",
                    "monitoring"
                )
                val device = when {
                    ua.containsAny("ipad", "tablet") -> "TABLET"
                    ua.containsAny("mobile", "iphone", "android") -> "MOBILE"
                    bot -> "BOT"
                    else -> "DESKTOP"
                }
                val browser = when {
                    "edg/" in ua -> "EDGE"; "firefox/" in ua -> "FIREFOX"; ua.containsAny(
                        "chrome/",
                        "crios/"
                    ) -> "CHROME"

                    "safari/" in ua -> "SAFARI"; "curl/" in ua -> "CURL"; bot -> "BOT"; else -> "UNKNOWN"
                }
                val os = when {
                    "windows" in ua -> "WINDOWS"; ua.containsAny(
                        "iphone",
                        "ipad",
                        "ios"
                    ) -> "IOS"; "android" in ua -> "ANDROID"
                    ua.containsAny("mac os", "macintosh") -> "MACOS"; "linux" in ua -> "LINUX"; else -> "UNKNOWN"
                }
                return UserAgentInfo(browser, device, os, bot)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AnalyticsService::class.java)
        private val DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC)
    }
}

private fun String.containsAny(vararg needles: String) = needles.any(::contains)
