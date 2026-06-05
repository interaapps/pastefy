package de.interaapps.pastefy.infrastructure.elastic.migrations

import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.StringReader

@Component
class ElasticV1PasteIndexMigration : ElasticMigration {
    override val version = 1

    override fun migrate(context: ElasticMigrationContext) {
        val index = context.properties.indexName.requireElasticName("index")
        if (indexExists(context, index)) {
            context.client.indices().putMapping { request ->
                request.index(index).withJson(StringReader(LEGACY_MAPPING_JSON))
            }
            LOGGER.info("Updated Elasticsearch legacy mapping for index {}", index)
            return
        }

        createIndex(context, index)
    }

    private fun createIndex(context: ElasticMigrationContext, index: String) {
        try {
            context.client.indices().create { request ->
                request.index(index)
                    .settings { settings ->
                        settings
                            .numberOfShards(context.properties.numberOfShards.coerceAtLeast(1).toString())
                            .numberOfReplicas(context.properties.numberOfReplicas.coerceAtLeast(0).toString())
                    }
                    .mappings(MAPPING)
            }
            LOGGER.info("Created Elasticsearch index {}", index)
        } catch (exception: RuntimeException) {
            if (!indexExists(context, index)) throw exception
            LOGGER.info("Elasticsearch index {} was created concurrently", index)
        }
    }

    private fun indexExists(context: ElasticMigrationContext, index: String): Boolean =
        context.client.indices().exists { it.index(index) }.value()

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ElasticV1PasteIndexMigration::class.java)
        private val LEGACY_MAPPING_JSON =
            """
            {
              "properties": {
                "key": { "type": "keyword" },
                "title": { "type": "text", "fields": { "keyword": { "type": "keyword" } } },
                "content": { "type": "text" },
                "version": { "type": "integer" },
                "starCount": { "type": "integer" },
                "engagementScore": { "type": "integer" },
                "userId": { "type": "keyword" },
                "forkedFrom": { "type": "keyword" },
                "visibility": { "type": "keyword" },
                "folder": { "type": "keyword" },
                "type": { "type": "keyword" },
                "storageType": { "type": "keyword" },
                "tags": { "type": "keyword" },
                "starredBy": { "type": "keyword" },
                "encrypted": { "type": "boolean" },
                "expireAt": { "type": "date" },
                "createdAt": { "type": "date" },
                "updatedAt": { "type": "date" },
                "user": {
                  "properties": {
                    "id": { "type": "keyword" },
                    "type": { "type": "keyword" },
                    "authId": { "type": "keyword" },
                    "authProvider": { "type": "keyword" },
                    "avatar": { "type": "text" },
                    "name": { "type": "text", "fields": { "keyword": { "type": "keyword" } } },
                    "uniqueName": { "type": "text", "fields": { "keyword": { "type": "keyword" } } },
                    "eMail": { "type": "text", "fields": { "keyword": { "type": "keyword" } } },
                    "createdAt": { "type": "date" },
                    "updatedAt": { "type": "date" }
                  }
                }
              }
            }
            """.trimIndent()

        private val MAPPING = TypeMapping.of { builder ->
            builder.withJson(
                StringReader(LEGACY_MAPPING_JSON),
            )
        }
    }
}
