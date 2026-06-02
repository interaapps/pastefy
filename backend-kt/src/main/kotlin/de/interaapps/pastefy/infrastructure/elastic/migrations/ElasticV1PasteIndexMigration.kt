package de.interaapps.pastefy.infrastructure.elastic.migrations

import co.elastic.clients.elasticsearch._types.Conflicts
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping
import co.elastic.clients.elasticsearch.indices.update_aliases.Action
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.StringReader

@Component
class ElasticV1PasteIndexMigration : ElasticMigration {
    override val version = 1

    override fun migrate(context: ElasticMigrationContext) {
        val target = context.versionedIndex(version)
        createIndexIfMissing(context, target)

        val aliasedIndices = aliasIndices(context)
        if (aliasedIndices == setOf(target)) return

        val sources = when {
            aliasedIndices.isNotEmpty() -> aliasedIndices
            context.legacyIndex != target && indexExists(context, context.legacyIndex) -> setOf(context.legacyIndex)
            else -> emptySet()
        }
        sources.filterNot { it == target }.forEach { source -> reindex(context, source, target) }
        switchAlias(context, aliasedIndices, target)
    }

    private fun createIndexIfMissing(context: ElasticMigrationContext, index: String) {
        if (indexExists(context, index)) return
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

    private fun reindex(context: ElasticMigrationContext, source: String, target: String) {
        LOGGER.info("Reindexing Elasticsearch index {} into {}", source, target)
        val response = context.client.reindex { request ->
            request
                .source { it.index(source) }
                .dest { it.index(target) }
                .conflicts(Conflicts.Abort)
                .refresh(true)
                .waitForCompletion(true)
        }
        check(response.timedOut() != true) { "Elasticsearch reindex operation timed out: $source -> $target" }
        check(response.failures().isEmpty()) {
            "Elasticsearch reindex operation failed: ${response.failures().joinToString()}"
        }
        LOGGER.info("Reindexed {} Elasticsearch document(s) into {}", response.created(), target)
    }

    private fun switchAlias(context: ElasticMigrationContext, previousIndices: Set<String>, target: String) {
        val actions = previousIndices.filterNot { it == target }.map { index ->
            Action.of { action -> action.remove { it.index(index).alias(context.alias) } }
        } + Action.of { action ->
            action.add { it.index(target).alias(context.alias).isWriteIndex(true) }
        }
        context.client.indices().updateAliases { it.actions(actions) }
        LOGGER.info("Elasticsearch alias {} now points to {}", context.alias, target)
    }

    private fun aliasIndices(context: ElasticMigrationContext): Set<String> {
        val indices = context.client.indices()
        if (!indices.existsAlias { it.name(context.alias) }.value()) return emptySet()
        return indices.getAlias { it.name(context.alias) }.result().keys
    }

    private fun indexExists(context: ElasticMigrationContext, index: String): Boolean =
        context.client.indices().exists { it.index(index) }.value()

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ElasticV1PasteIndexMigration::class.java)
        private val MAPPING = TypeMapping.of { builder ->
            builder.withJson(
                StringReader(
                    """
                    {
                      "dynamic": "strict",
                      "properties": {
                        "documentId": { "type": "keyword" },
                        "id": { "type": "integer" },
                        "key": { "type": "keyword" },
                        "title": { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
                        "content": { "type": "text" },
                        "userId": { "type": "keyword" },
                        "forkedFrom": { "type": "keyword" },
                        "encrypted": { "type": "boolean" },
                        "folder": { "type": "keyword" },
                        "type": { "type": "keyword" },
                        "visibility": { "type": "keyword" },
                        "expireAt": { "type": "date" },
                        "createdAt": { "type": "date" },
                        "updatedAt": { "type": "date" },
                        "storageType": { "type": "keyword" },
                        "version": { "type": "integer" },
                        "engagementScore": { "type": "integer" },
                        "tags": { "type": "keyword" },
                        "starCount": { "type": "integer" },
                        "starredBy": { "type": "keyword" },
                        "user": {
                          "properties": {
                            "id": { "type": "keyword" },
                            "name": { "type": "text", "fields": { "keyword": { "type": "keyword", "ignore_above": 256 } } },
                            "uniqueName": { "type": "keyword" },
                            "email": { "type": "keyword" },
                            "avatar": { "type": "keyword", "index": false }
                          }
                        }
                      }
                    }
                    """.trimIndent(),
                ),
            )
        }
    }
}
