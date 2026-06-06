package de.interaapps.pastefy.infrastructure.elastic.migrations

import co.elastic.clients.elasticsearch.ElasticsearchClient
import de.interaapps.pastefy.config.PastefyProperties
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component("elasticMigrationRunner")
@ConditionalOnProperty(prefix = "pastefy.elasticsearch", name = ["enabled"], havingValue = "true")
class ElasticMigrationRunner(
    private val client: ElasticsearchClient,
    private val properties: PastefyProperties,
    private val migrations: List<ElasticMigration>,
) {
    @PostConstruct
    fun migrate() {
        if (!properties.elasticsearch.migrations.enabled) {
            LOGGER.info("Elasticsearch migrations are disabled")
            return
        }

        val context = ElasticMigrationContext(client, properties.elasticsearch)
        migrations.sortedBy(ElasticMigration::version).forEach { migration ->
            LOGGER.info("Applying Elasticsearch migration V{}", migration.version)
            migration.migrate(context)
        }
        LOGGER.info("Elasticsearch migrations are up to date")
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ElasticMigrationRunner::class.java)
    }
}
