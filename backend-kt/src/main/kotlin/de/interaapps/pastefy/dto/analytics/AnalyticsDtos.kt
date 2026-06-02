package de.interaapps.pastefy.dto.analytics

import jakarta.servlet.http.HttpServletRequest
import java.time.Instant
import java.time.temporal.ChronoUnit

data class AnalyticsResponse(
    var totalVisits: Long = 0,
    var uniqueVisitors: Long = 0,
    var botVisits: Long = 0,
    var botTrackingEnabled: Boolean = false,
    val series: MutableList<SeriesPoint> = mutableListOf(),
    val breakdown: MutableList<BreakdownPoint> = mutableListOf(),
)

data class SeriesPoint(val bucket: String, val visits: Long, val uniqueVisitors: Long)
data class BreakdownPoint(val value: String, val visits: Long, val uniqueVisitors: Long)

data class AnalyticsQuery(
    var from: Instant = Instant.now().minus(30, ChronoUnit.DAYS),
    var to: Instant = Instant.now(),
    var interval: String = "day",
    var groupBy: String = "country",
    var includeSummary: Boolean = true,
    var includeBreakdown: Boolean = true,
    val filters: MutableMap<String, String> = linkedMapOf(),
) {
    companion object {
        val FILTERS = setOf(
            "paste_key", "paste_visibility", "paste_user_id", "visit_type", "country", "region", "city",
            "visitor_user_id", "browser", "device_type", "os", "referer_host", "acquisition", "is_bot",
        )

        fun from(request: HttpServletRequest): AnalyticsQuery {
            val result = AnalyticsQuery()
            result.from = request.getParameter("from")?.toInstantOrNull() ?: result.from
            result.to = request.getParameter("to")?.toInstantOrNull() ?: result.to
            if (result.from.isAfter(result.to)) {
                val from = result.from
                result.from = result.to
                result.to = from
            }
            result.interval = request.getParameter("interval")?.takeIf { it in setOf("hour", "day", "week", "month") } ?: "day"
            result.groupBy = request.getParameter("group_by")?.takeIf { it in FILTERS } ?: "country"
            result.includeSummary = !request.getParameter("include_summary").equals("false", ignoreCase = true)
            result.includeBreakdown = !request.getParameter("include_breakdown").equals("false", ignoreCase = true)
            FILTERS.forEach { field ->
                request.getParameter(field)?.trim()?.takeIf(String::isNotBlank)?.let { result.filters[field] = it }
            }
            return result
        }

        private fun String.toInstantOrNull() = runCatching { Instant.parse(this) }.getOrNull()
    }
}
