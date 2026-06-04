package de.interaapps.pastefy.infrastructure.analytics

import de.interaapps.pastefy.config.PastefyProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
@ConditionalOnProperty(prefix = "pastefy.analytics", name = ["enabled"], havingValue = "true")
class AnalyticsConfiguration {
    @Bean
    fun analyticsJdbcTemplate(properties: PastefyProperties): JdbcTemplate =
        JdbcTemplate(analyticsDataSource(properties))

    private fun analyticsDataSource(properties: PastefyProperties): DriverManagerDataSource {
        require(properties.analytics.jdbcUrl.isNotBlank()) { "pastefy.analytics.jdbc-url is required when analytics are enabled" }
        require(properties.analytics.ipHashSalt.isNotBlank()) { "pastefy.analytics.ip-hash-salt is required when analytics are enabled" }
        return DriverManagerDataSource().apply {
            setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver")
            url = properties.analytics.jdbcUrl
            username = properties.analytics.user
            password = properties.analytics.password
        }
    }
}
