package de.interaapps.pastefy.infrastructure.elastic

import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.enums.StorageType
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.WriteTypeHint
import java.time.Instant

@Document(indexName = "pastefy_pastes_current", createIndex = false, writeTypeHint = WriteTypeHint.FALSE)
data class ElasticPasteDocument(
    @Id
    val documentId: String = "",
    //val id: Int = 0,
    val key: String = "",
    val title: String? = null,
    val content: String? = null,
    val userId: String? = null,
    val forkedFrom: String? = null,
    val encrypted: Boolean = false,
    val folder: String? = null,
    val type: PasteType = PasteType.PASTE,
    val visibility: PasteVisibility = PasteVisibility.UNLISTED,
    val expireAt: Instant? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
    val storageType: StorageType = StorageType.DATABASE,
    val version: Int = 0,
    val engagementScore: Int = 0,
    val tags: List<String> = emptyList(),
    val starCount: Int = 0,
    val starredBy: List<String> = emptyList(),
    val user: ElasticUserDocument? = null,
)

data class ElasticUserDocument(
    val id: String,
    val name: String? = null,
    val uniqueName: String? = null,
    val email: String? = null,
    val avatar: String? = null,
)
