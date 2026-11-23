package de.interaapps.pastefy.infrastructure.elastic.migrations

import co.elastic.clients.elasticsearch.ElasticsearchClient
import de.interaapps.pastefy.config.PastefyProperties

interface ElasticMigration {
    val version: Int
    fun migrate(context: ElasticMigrationContext)
}

data class ElasticMigrationContext(
    val client: ElasticsearchClient,
    val properties: PastefyProperties.Elasticsearch,
) {
    val alias: String = properties.indexName.requireElasticName("index alias")
    val indexPrefix: String = properties.indexPrefix.requireElasticName("index prefix")
    val legacyIndex: String = properties.legacyIndexName.requireElasticName("legacy index")

    fun versionedIndex(version: Int): String = "$indexPrefix-v$version"
}

internal fun String.requireElasticName(description: String): String =
    takeIf { matches(Regex("[a-z0-9][a-z0-9._-]*")) }
        ?: throw IllegalArgumentException("Invalid Elasticsearch $description: $this")
