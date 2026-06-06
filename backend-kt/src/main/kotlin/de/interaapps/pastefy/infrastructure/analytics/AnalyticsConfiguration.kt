package de.interaapps.pastefy.infrastructure.analytics

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.interaapps.pastefy.config.PastefyProperties
import jakarta.annotation.PreDestroy
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
@ConditionalOnProperty(prefix = "pastefy.analytics", name = ["enabled"], havingValue = "true")
class AnalyticsConfiguration {
    private var analyticsDataSource: HikariDataSource? = null

    @Bean
    fun analyticsJdbcTemplate(properties: PastefyProperties): JdbcTemplate {
        analyticsDataSource = analyticsDataSource(properties)
        return JdbcTemplate(requireNotNull(analyticsDataSource))
    }

    private fun analyticsDataSource(properties: PastefyProperties): HikariDataSource {
        require(properties.analytics.jdbcUrl.isNotBlank()) { "pastefy.analytics.jdbc-url is required when analytics are enabled" }
        require(properties.analytics.ipHashSalt.isNotBlank()) { "pastefy.analytics.ip-hash-salt is required when analytics are enabled" }

        val pool = properties.analytics.pool
        val config = HikariConfig().apply {
            poolName = "pastefy-clickhouse-analytics"
            driverClassName = "com.clickhouse.jdbc.ClickHouseDriver"
            jdbcUrl = clickHouseJdbcUrl(properties.analytics.jdbcUrl, properties.analytics.database)
            username = properties.analytics.user
            password = properties.analytics.password
            maximumPoolSize = pool.maximumPoolSize.coerceAtLeast(1)
            minimumIdle = pool.minimumIdle.coerceIn(0, maximumPoolSize)
            connectionTimeout = pool.connectionTimeoutMillis.coerceAtLeast(250)
            idleTimeout = pool.idleTimeoutMillis.coerceAtLeast(0)
            maxLifetime = pool.maxLifetimeMillis.coerceAtLeast(30_000)
            validationTimeout = pool.validationTimeoutMillis.coerceAtLeast(250)
            connectionTestQuery = "SELECT 1"
        }
        return HikariDataSource(config)
    }

    @PreDestroy
    fun closeAnalyticsDataSource() {
        analyticsDataSource?.close()
    }
}
