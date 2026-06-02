package de.interaapps.pastefy.infrastructure.analytics

import de.interaapps.pastefy.config.PastefyProperties
import org.flywaydb.core.Flyway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource

@Configuration
@ConditionalOnProperty(prefix = "pastefy.analytics.migrations", name = ["enabled"], havingValue = "true")
class ClickHouseFlywayConfiguration {
    @Bean(initMethod = "migrate")
    fun clickHouseFlyway(properties: PastefyProperties): Flyway =
        Flyway.configure()
            .dataSource(
                DriverManagerDataSource().apply {
                    require(properties.analytics.jdbcUrl.isNotBlank()) {
                        "pastefy.analytics.jdbc-url is required when ClickHouse migrations are enabled"
                    }
                    setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver")
                    url = properties.analytics.jdbcUrl
                    username = properties.analytics.user
                    password = properties.analytics.password
                },
            )
            .locations("classpath:db/migration/clickhouse")
            .placeholders(
                mapOf(
                    "analyticsDatabase" to identifier(properties.analytics.database),
                    "analyticsTable" to identifier(properties.analytics.table),
                    "retentionDays" to properties.analytics.retentionDays.coerceAtLeast(1).toString(),
                ),
            )
            .load()

    private fun identifier(value: String): String =
        value.takeIf { it.matches(Regex("[A-Za-z_][A-Za-z0-9_]*")) }
            ?: throw IllegalArgumentException("Invalid ClickHouse identifier: $value")
}
