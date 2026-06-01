package de.interaapps.pastefy.analytics;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.maxmind.db.CHMCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.webutils.config.Config;

import java.io.File;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AnalyticsService {
    public static final Set<String> GROUPABLE_FIELDS = Set.of(
            "paste_key", "paste_visibility", "paste_user_id", "visit_type",
            "country", "region", "city", "visitor_user_id", "browser", "device_type", "os",
            "referer_host", "acquisition", "is_bot"
    );

    private static final DateTimeFormatter CLICKHOUSE_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC);
    private static final int MAX_REFERER_HOST_LENGTH = 253;
    private static final Logger LOGGER = Logger.getLogger(AnalyticsService.class.getName());
    private static final Gson GSON = new Gson();

    private final HttpClient httpClient;
    private final URI clickHouseUrl;
    private final String database;
    private final String table;
    private final String authHeader;
    private final String ipSalt;
    private final String ipSource;
    private final String ipHeader;
    private final boolean trackBots;
    private final int batchSize;
    private final Duration requestTimeout;
    private final BlockingQueue<VisitEvent> queue;
    private final ScheduledExecutorService writer;
    private final DatabaseReader geoIpReader;
    private final AtomicLong droppedEvents = new AtomicLong();
    private final AtomicLong writerFailures = new AtomicLong();

    public enum VisitType {
        PAGE, RAW
    }

    public static AnalyticsService create(Pastefy pastefy) {
        Config config = pastefy.getConfig();
        if (!config.has("analytics.clickhouse.url")) return null;
        if (!config.has("analytics.iphashsalt") || config.get("analytics.iphashsalt", "").isBlank()) {
            LOGGER.warning("Analytics disabled: ANALYTICS_IP_HASH_SALT must be configured.");
            return null;
        }
        try {
            return new AnalyticsService(config);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Analytics disabled: initialization failed", exception);
            return null;
        }
    }

    private AnalyticsService(Config config) {
        clickHouseUrl = URI.create(config.get("analytics.clickhouse.url"));
        database = identifier(config.get("analytics.clickhouse.database", "default"));
        table = identifier(config.get("analytics.clickhouse.table", "pastefy_analytics_visits"));
        ipSalt = config.get("analytics.iphashsalt");
        ipSource = config.get("analytics.ipsource", "direct").toLowerCase(Locale.ROOT);
        ipHeader = config.get("analytics.ipheader", "");
        trackBots = config.get("analytics.trackbots", "true").equalsIgnoreCase("true");
        batchSize = Math.max(1, config.getInt("analytics.batchsize", 1000));
        queue = new ArrayBlockingQueue<>(Math.max(batchSize, config.getInt("analytics.queuecapacity", 100000)));
        requestTimeout = Duration.ofMillis(config.getInt("analytics.http.requesttimeoutmillis", 5000));
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getInt("analytics.http.connecttimeoutmillis", 2000)))
                .build();

        String user = config.get("analytics.clickhouse.user", "default");
        String password = config.get("analytics.clickhouse.password", "");
        authHeader = "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
        geoIpReader = createGeoIpReader(config.get("analytics.geoip.mmdbpath", ""));

        if (config.get("analytics.clickhouse.automigrate", "true").equalsIgnoreCase("true")) {
            initializeSchema(Math.max(1, config.getInt("analytics.retentiondays", 90)));
        }

        writer = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "pastefy-analytics-writer");
            thread.setDaemon(true);
            return thread;
        });
        int flushIntervalMillis = Math.max(1, config.getInt("analytics.flushintervalmillis", 1000));
        writer.scheduleWithFixedDelay(this::flushSafely, flushIntervalMillis, flushIntervalMillis, TimeUnit.MILLISECONDS);
    }

    public void track(Exchange exchange, Paste paste, User visitor, VisitType visitType) {
        UserAgentInfo userAgent = UserAgentInfo.parse(exchange.header("User-Agent"));
        if (!trackBots && userAgent.bot) return;

        String ip = resolveIp(exchange);
        GeoLocation geo = lookupGeo(ip);
        String refererHost = refererHost(exchange.header("Referer"));

        VisitEvent event = new VisitEvent();
        event.paste_key = paste.getKey();
        event.paste_visibility = paste.getVisibility().name();
        event.paste_user_id = paste.getUserId();
        event.visit_type = visitType.name();
        event.visited_at = CLICKHOUSE_DATE_TIME.format(Instant.now());
        event.country = geo.country;
        event.region = geo.region;
        event.city = geo.city;
        event.visitor_user_id = visitor == null ? null : visitor.getId();
        event.browser = userAgent.browser;
        event.device_type = userAgent.deviceType;
        event.os = userAgent.os;
        event.ip_hash = hashIp(ip);
        event.referer_host = refererHost;
        event.acquisition = acquisition(refererHost, exchange.header("Host"));
        event.is_bot = userAgent.bot ? 1 : 0;

        if (!queue.offer(event)) {
            logDroppedEvent();
        }
    }

    public AnalyticsResponse query(AnalyticsQuery query) {
        if (!trackBots) {
            query.filters.remove("is_bot");
            if (query.groupBy.equals("is_bot")) query.groupBy = "country";
        }
        String where = buildWhere(query);
        AnalyticsResponse response = new AnalyticsResponse();
        response.botTrackingEnabled = trackBots;

        if (query.includeSummary) {
            String botVisits = trackBots ? ", countIf(is_bot = 1) AS bot_visits" : "";
            JsonObject totals = firstRow(select("SELECT count() AS total_visits, uniqExact(ip_hash) AS unique_visitors"
                    + botVisits + " FROM " + fullTable() + where + " FORMAT JSONEachRow"));
            if (totals != null) {
                response.totalVisits = totals.get("total_visits").getAsLong();
                response.uniqueVisitors = totals.get("unique_visitors").getAsLong();
                if (trackBots) response.botVisits = totals.get("bot_visits").getAsLong();
            }

            String bucket = switch (query.interval) {
                case "hour" -> "toStartOfHour(visited_at)";
                case "week" -> "toStartOfWeek(visited_at)";
                case "month" -> "toStartOfMonth(visited_at)";
                default -> "toStartOfDay(visited_at)";
            };
            for (JsonObject row : rows(select("SELECT formatDateTime(" + bucket + ", '%FT%TZ', 'UTC') AS bucket, "
                    + "count() AS visits, uniqExact(ip_hash) AS unique_visitors FROM " + fullTable() + where
                    + " GROUP BY bucket ORDER BY bucket FORMAT JSONEachRow"))) {
                AnalyticsResponse.SeriesPoint point = new AnalyticsResponse.SeriesPoint();
                point.bucket = row.get("bucket").getAsString();
                point.visits = row.get("visits").getAsLong();
                point.uniqueVisitors = row.get("unique_visitors").getAsLong();
                response.series.add(point);
            }
        }

        if (query.includeBreakdown) {
            for (JsonObject row : rows(select("SELECT toString(" + query.groupBy + ") AS value, count() AS visits, "
                    + "uniqExact(ip_hash) AS unique_visitors FROM " + fullTable() + where
                    + " GROUP BY value ORDER BY visits DESC LIMIT 25 FORMAT JSONEachRow"))) {
                AnalyticsResponse.BreakdownPoint point = new AnalyticsResponse.BreakdownPoint();
                point.value = row.get("value").getAsString();
                point.visits = row.get("visits").getAsLong();
                point.uniqueVisitors = row.get("unique_visitors").getAsLong();
                response.breakdown.add(point);
            }
        }
        return response;
    }

    private void initializeSchema(int retentionDays) {
        execute("CREATE TABLE IF NOT EXISTS " + fullTable() + " ("
                + "paste_key FixedString(8), paste_visibility LowCardinality(String), paste_user_id Nullable(FixedString(8)), "
                + "visit_type LowCardinality(String), visited_at DateTime64(3, 'UTC') CODEC(DoubleDelta, ZSTD(1)), "
                + "country LowCardinality(String), region LowCardinality(String), city LowCardinality(String), "
                + "visitor_user_id Nullable(FixedString(8)), browser LowCardinality(String), device_type LowCardinality(String), "
                + "os LowCardinality(String), ip_hash Nullable(UInt64), referer_host String, "
                + "acquisition LowCardinality(String), is_bot UInt8"
                + ") ENGINE = MergeTree PARTITION BY toYYYYMM(visited_at) ORDER BY (paste_key, visited_at) "
                + "TTL visited_at + INTERVAL " + retentionDays + " DAY DELETE");
    }

    private void flushSafely() {
        do {
            List<VisitEvent> batch = new ArrayList<>(batchSize);
            queue.drainTo(batch, batchSize);
            if (batch.isEmpty()) return;
            try {
                StringBuilder body = new StringBuilder();
                batch.forEach(event -> body.append(GSON.toJson(event)).append('\n'));
                execute("INSERT INTO " + fullTable() + " FORMAT JSONEachRow", body.toString());
                writerFailures.set(0);
            } catch (RuntimeException exception) {
                long failures = writerFailures.incrementAndGet();
                if (failures == 1 || failures % 60 == 0) {
                    LOGGER.log(Level.WARNING, "Could not write analytics batch (" + failures + " consecutive failure(s))", exception);
                }
                batch.forEach(event -> {
                    if (!queue.offer(event)) logDroppedEvent();
                });
                return;
            }
        } while (queue.size() >= batchSize);
    }

    private void logDroppedEvent() {
        long dropped = droppedEvents.incrementAndGet();
        if (dropped == 1 || dropped % 1000 == 0) {
            LOGGER.warning("Analytics queue is full; dropped " + dropped + " visit event(s).");
        }
    }

    private String buildWhere(AnalyticsQuery query) {
        List<String> clauses = new ArrayList<>();
        clauses.add("visited_at >= parseDateTime64BestEffort('" + CLICKHOUSE_DATE_TIME.format(query.from) + "')");
        clauses.add("visited_at <= parseDateTime64BestEffort('" + CLICKHOUSE_DATE_TIME.format(query.to) + "')");
        if (!trackBots) clauses.add("is_bot = 0");
        query.filters.forEach((field, value) -> {
            if (field.equals("is_bot")) {
                clauses.add("is_bot = " + (Boolean.parseBoolean(value) || value.equals("1") ? "1" : "0"));
            } else {
                clauses.add(field + " = '" + escape(value) + "'");
            }
        });
        return " WHERE " + String.join(" AND ", clauses);
    }

    private String select(String sql) {
        return execute(sql, "");
    }

    private void execute(String sql) {
        execute(sql, "");
    }

    private String execute(String sql, String body) {
        try {
            String separator = clickHouseUrl.toString().contains("?") ? "&" : "?";
            URI uri = URI.create(clickHouseUrl + separator + "database=" + url(database)
                    + "&query=" + url(sql));
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(requestTimeout)
                    .header("Authorization", authHeader)
                    .header("Content-Type", "text/plain; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("ClickHouse returned " + response.statusCode() + ": " + response.body());
            }
            return response.body();
        } catch (Exception exception) {
            throw new IllegalStateException("ClickHouse request failed", exception);
        }
    }

    private String resolveIp(Exchange exchange) {
        String value;
        if (!ipHeader.isBlank()) {
            value = exchange.header(ipHeader);
        } else {
            value = switch (ipSource) {
                case "x-forwarded-for", "xff" -> exchange.header("X-Forwarded-For");
                case "cloudflare", "cf-connecting-ip" -> exchange.header("CF-Connecting-IP");
                default -> exchange.remoteAddr();
            };
        }
        if (value == null || value.isBlank()) return "";
        value = value.split(",")[0].trim();
        if (value.startsWith("[") && value.contains("]")) return value.substring(1, value.indexOf(']'));
        if (value.chars().filter(character -> character == ':').count() == 1 && value.contains(".")) {
            return value.substring(0, value.indexOf(':'));
        }
        return value;
    }

    private BigInteger hashIp(String ip) {
        if (ip.isBlank()) return null;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((ipSalt + ":" + ip).getBytes(StandardCharsets.UTF_8));
            return new BigInteger(1, Arrays.copyOf(digest, Long.BYTES));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private GeoLocation lookupGeo(String ip) {
        if (geoIpReader == null || ip.isBlank()) return new GeoLocation();
        try {
            CityResponse response = geoIpReader.tryCity(InetAddress.getByName(ip)).orElse(null);
            if (response == null) return new GeoLocation();
            GeoLocation location = new GeoLocation();
            location.country = nonNull(response.getCountry().getIsoCode());
            location.region = nonNull(response.getMostSpecificSubdivision().getIsoCode());
            location.city = nonNull(response.getCity().getName());
            return location;
        } catch (Exception ignored) {
            return new GeoLocation();
        }
    }

    private DatabaseReader createGeoIpReader(String path) {
        if (path == null || path.isBlank()) return null;
        try {
            return new DatabaseReader.Builder(new File(path)).withCache(new CHMCache()).build();
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, "GeoIP database could not be loaded; location fields stay empty", exception);
            return null;
        }
    }

    private static String refererHost(String value) {
        if (value == null || value.isBlank()) return "";
        try {
            String host = URI.create(value).getHost();
            if (host == null || host.length() > MAX_REFERER_HOST_LENGTH) return "";
            return host.toLowerCase(Locale.ROOT);
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private static String acquisition(String refererHost, String requestHost) {
        if (refererHost.isBlank()) return "DIRECT";
        String ownHost = requestHost == null ? "" : requestHost.split(":")[0].toLowerCase(Locale.ROOT);
        if (refererHost.equals(ownHost)) return "INTERNAL";
        if (containsAny(refererHost, "google.", "bing.", "duckduckgo.", "yahoo.", "ecosia.", "brave.")) return "ORGANIC_SEARCH";
        if (containsAny(refererHost, "github.com", "gitlab.com")) return "DEVELOPER_REFERRAL";
        if (containsAny(refererHost, "twitter.com", "x.com", "facebook.com", "linkedin.com", "reddit.com", "mastodon.", "bsky.app")) return "SOCIAL";
        return "REFERRAL";
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }

    private String fullTable() {
        return database + "." + table;
    }

    private static String identifier(String value) {
        if (value == null || !value.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid ClickHouse identifier: " + value);
        }
        return value;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static String url(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }

    private static JsonObject firstRow(String body) {
        List<JsonObject> rows = rows(body);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static List<JsonObject> rows(String body) {
        List<JsonObject> result = new ArrayList<>();
        if (body == null || body.isBlank()) return result;
        for (String line : body.split("\\R")) {
            if (!line.isBlank()) result.add(GSON.fromJson(line, JsonObject.class));
        }
        return result;
    }

    private static class VisitEvent {
        String paste_key;
        String paste_visibility;
        String paste_user_id;
        String visit_type;
        String visited_at;
        String country;
        String region;
        String city;
        String visitor_user_id;
        String browser;
        String device_type;
        String os;
        BigInteger ip_hash;
        String referer_host;
        String acquisition;
        int is_bot;
    }

    private static class GeoLocation {
        String country = "";
        String region = "";
        String city = "";
    }

    private static class UserAgentInfo {
        String browser = "UNKNOWN";
        String deviceType = "DESKTOP";
        String os = "UNKNOWN";
        boolean bot;

        static UserAgentInfo parse(String raw) {
            UserAgentInfo result = new UserAgentInfo();
            String userAgent = raw == null ? "" : raw.toLowerCase(Locale.ROOT);
            result.bot = containsAny(userAgent, "bot", "crawler", "spider", "slurp", "preview", "wget", "curl/", "httpclient", "python-requests", "uptime", "monitoring");
            if (containsAny(userAgent, "ipad", "tablet")) result.deviceType = "TABLET";
            else if (containsAny(userAgent, "mobile", "iphone", "android")) result.deviceType = "MOBILE";
            else if (result.bot) result.deviceType = "BOT";

            if (userAgent.contains("edg/")) result.browser = "EDGE";
            else if (userAgent.contains("firefox/")) result.browser = "FIREFOX";
            else if (userAgent.contains("chrome/") || userAgent.contains("crios/")) result.browser = "CHROME";
            else if (userAgent.contains("safari/")) result.browser = "SAFARI";
            else if (userAgent.contains("curl/")) result.browser = "CURL";
            else if (result.bot) result.browser = "BOT";

            if (userAgent.contains("windows")) result.os = "WINDOWS";
            else if (containsAny(userAgent, "iphone", "ipad", "ios")) result.os = "IOS";
            else if (userAgent.contains("android")) result.os = "ANDROID";
            else if (containsAny(userAgent, "mac os", "macintosh")) result.os = "MACOS";
            else if (userAgent.contains("linux")) result.os = "LINUX";
            return result;
        }
    }
}
