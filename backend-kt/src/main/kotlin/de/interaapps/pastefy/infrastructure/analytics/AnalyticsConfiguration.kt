package de.interaapps.pastefy.infrastructure.analytics

import de.interaapps.pastefy.config.PastefyProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@Configuration
@ConditionalOnProperty(prefix = "pastefy.analytics", name = ["enabled"], havingValue = "true")
class AnalyticsConfiguration {
    @Bean
    fun analyticsDataSource(properties: PastefyProperties): DataSource {
        require(properties.analytics.jdbcUrl.isNotBlank()) { "pastefy.analytics.jdbc-url is required when analytics are enabled" }
        require(properties.analytics.ipHashSalt.isNotBlank()) { "pastefy.analytics.ip-hash-salt is required when analytics are enabled" }
        return DriverManagerDataSource().apply {
            setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver")
            url = properties.analytics.jdbcUrl
            username = properties.analytics.user
            password = properties.analytics.password
        }
    }

    @Bean
    fun analyticsJdbcTemplate(@Qualifier("analyticsDataSource") analyticsDataSource: DataSource): JdbcTemplate =
        JdbcTemplate(analyticsDataSource)
}
