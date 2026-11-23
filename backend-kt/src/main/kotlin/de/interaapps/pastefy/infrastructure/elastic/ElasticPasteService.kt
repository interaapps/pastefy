package de.interaapps.pastefy.infrastructure.elastic

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.enums.StorageType
import de.interaapps.pastefy.repositories.PasteStarRepository
import de.interaapps.pastefy.repositories.PasteTagRepository
import de.interaapps.pastefy.repositories.PublicPasteEngagementRepository
import de.interaapps.pastefy.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.DependsOn
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.stereotype.Service

@Service
@DependsOn("elasticMigrationRunner")
@ConditionalOnProperty(prefix = "pastefy.elasticsearch", name = ["enabled"], havingValue = "true")
class ElasticPasteService(
    private val operations: ElasticsearchOperations,
    private val properties: PastefyProperties,
    private val pasteTagRepository: PasteTagRepository,
    private val pasteStarRepository: PasteStarRepository,
    private val engagementRepository: PublicPasteEngagementRepository,
    private val userRepository: UserRepository,
) {
    private val logger = LoggerFactory.getLogger(ElasticPasteService::class.java)
    private val indexCoordinates: IndexCoordinates
        get() = IndexCoordinates.of(properties.elasticsearch.indexName)

    fun store(paste: Paste, content: String?): Boolean = runCatching {
        operations.save(toDocument(paste, content), indexCoordinates)
        true
    }.onFailure {
        logger.error("Unable to index paste {}", paste.id, it)
    }.getOrDefault(false)

    fun delete(paste: Paste) {
        runCatching {
            operations.delete(requireNotNull(paste.id).toString(), indexCoordinates)
        }.onFailure {
            logger.error("Unable to delete paste {} from Elasticsearch", paste.id, it)
        }
    }

    fun count(): Long = operations.count(Query.findAll(), indexCoordinates)

    fun updateTags(paste: Paste) = updateDocument(paste) { document ->
        document.copy(tags = tags(paste))
    }

    fun updateEngagement(paste: Paste) = updateDocument(paste) { document ->
        document.copy(engagementScore = engagement(paste))
    }

    fun updateStars(paste: Paste) = updateDocument(paste) { document ->
        val starredBy = starredBy(paste)
        document.copy(starCount = starredBy.size, starredBy = starredBy)
    }

    fun updateUser(paste: Paste) = updateDocument(paste) { document ->
        document.copy(user = user(paste))
    }

    private fun updateDocument(paste: Paste, update: (ElasticPasteDocument) -> ElasticPasteDocument) {
        runCatching {
            val id = requireNotNull(paste.id).toString()
            val document = operations.get(id, ElasticPasteDocument::class.java, indexCoordinates) ?: return
            operations.save(update(document), indexCoordinates)
        }.onFailure {
            logger.error("Unable to update Elasticsearch document for paste {}", paste.id, it)
        }
    }

    private fun toDocument(paste: Paste, content: String?) = ElasticPasteDocument(
        documentId = requireNotNull(paste.id).toString(),
        pasteId = requireNotNull(paste.id),
        key = paste.key,
        title = paste.title,
        content = content,
        userId = paste.userId,
        forkedFrom = paste.forkedFrom,
        encrypted = paste.encrypted,
        folder = paste.folder,
        type = paste.type ?: PasteType.PASTE,
        visibility = paste.visibility ?: PasteVisibility.UNLISTED,
        expireAt = paste.expireAt,
        createdAt = paste.createdAt,
        updatedAt = paste.updatedAt,
        storageType = paste.storageType ?: StorageType.DATABASE,
        version = paste.version ?: 1,
        engagementScore = engagement(paste),
        tags = tags(paste),
        starCount = starredBy(paste).size,
        starredBy = starredBy(paste),
        user = user(paste),
    )

    private fun tags(paste: Paste): List<String> = pasteTagRepository.findAllByPaste(paste.key).map { it.tag }

    private fun starredBy(paste: Paste): List<String> = pasteStarRepository.findAllByPaste(paste.key).map { it.userId }

    private fun engagement(paste: Paste): Int =
        paste.id?.let(engagementRepository::findFirstByPasteIdOrderByIdAsc)?.score ?: 0

    private fun user(paste: Paste): ElasticUserDocument? =
        paste.userId?.let(userRepository::findById)?.orElse(null)?.toDocument()

    private fun User.toDocument() = ElasticUserDocument(
        id = id,
        name = name,
        uniqueName = uniqueName,
        eMail = email,
        avatar = avatar,
        authId = authId,
        authProvider = authProvider,
        type = type,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
