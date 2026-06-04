package de.interaapps.pastefy.infrastructure.analytics

import java.net.URI

fun clickHouseJdbcUrl(raw: String, database: String): String {
    val value = raw.trim()
    if (value.startsWith("jdbc:", ignoreCase = true)) return value
    val uri = URI.create(value)
    require(uri.scheme == "http" || uri.scheme == "https") {
        "ClickHouse URL must be a jdbc:clickhouse, http or https URL"
    }
    val host = requireNotNull(uri.host) { "ClickHouse URL must contain a host" }
    val port = if (uri.port > 0) ":${uri.port}" else ""
    val ssl = if (uri.scheme == "https") "?ssl=true" else ""
    return "jdbc:clickhouse://$host$port/$database$ssl"
}
