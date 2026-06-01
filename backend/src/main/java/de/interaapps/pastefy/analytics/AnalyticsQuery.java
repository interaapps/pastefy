package de.interaapps.pastefy.analytics;

import org.javawebstack.http.router.Exchange;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AnalyticsQuery {
    private static final Set<String> FILTERS = Set.of(
            "paste_key", "paste_visibility", "paste_user_id", "visit_type",
            "country", "region", "city", "visitor_user_id", "browser", "device_type", "os",
            "referer_host", "acquisition", "is_bot"
    );

    public Instant from = Instant.now().minus(30, ChronoUnit.DAYS);
    public Instant to = Instant.now();
    public String interval = "day";
    public String groupBy = "country";
    public boolean includeSummary = true;
    public boolean includeBreakdown = true;
    public final Map<String, String> filters = new LinkedHashMap<>();

    public static AnalyticsQuery from(Exchange exchange) {
        AnalyticsQuery query = new AnalyticsQuery();
        query.from = parseInstant(exchange.query("from"), query.from);
        query.to = parseInstant(exchange.query("to"), query.to);
        if (query.from.isAfter(query.to)) {
            Instant swap = query.from;
            query.from = query.to;
            query.to = swap;
        }

        String interval = exchange.query("interval", "day");
        if (Set.of("hour", "day", "week", "month").contains(interval)) {
            query.interval = interval;
        }

        String groupBy = exchange.query("group_by", "country");
        if (AnalyticsService.GROUPABLE_FIELDS.contains(groupBy)) {
            query.groupBy = groupBy;
        }
        query.includeSummary = !"false".equalsIgnoreCase(exchange.query("include_summary", "true"));
        query.includeBreakdown = !"false".equalsIgnoreCase(exchange.query("include_breakdown", "true"));

        FILTERS.forEach(filter -> {
            String value = exchange.query(filter);
            if (value != null && !value.isBlank()) {
                query.filters.put(filter, value.trim());
            }
        });
        return query;
    }

    private static Instant parseInstant(String value, Instant fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Instant.parse(value);
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }
}
