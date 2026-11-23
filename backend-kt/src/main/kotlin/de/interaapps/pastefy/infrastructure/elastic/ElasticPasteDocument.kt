package de.interaapps.pastefy.infrastructure.elastic

import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.enums.StorageType
import de.interaapps.pastefy.entities.User
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.WriteTypeHint
import java.time.Instant

@Document(indexName = "pastefy_pastes", createIndex = false, writeTypeHint = WriteTypeHint.FALSE)
data class ElasticPasteDocument(
    @Id
    val documentId: String = "",
    @Field(name = "id", type = FieldType.Integer)
    val pasteId: Int = 0,
    val key: String = "",
    val title: String? = null,
    val content: String? = null,
    val userId: String? = null,
    val forkedFrom: String? = null,
    val encrypted: Boolean = false,
    val folder: String? = null,
    val type: PasteType = PasteType.PASTE,
    val visibility: PasteVisibility = PasteVisibility.UNLISTED,

    @Field(type = FieldType.Date, format = [DateFormat.epoch_millis])
    val expireAt: Instant? = null,

    @Field(type = FieldType.Date, format = [DateFormat.epoch_millis])
    val createdAt: Instant? = null,

    @Field(type = FieldType.Date, format = [DateFormat.epoch_millis])
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
    @Field(name = "eMail")
    val eMail: String? = null,
    val avatar: String? = null,
    val authId: String? = null,
    val authProvider: User.AuthenticationProvider? = null,
    val type: User.Type = User.Type.USER,

    @Field(type = FieldType.Date, format = [DateFormat.epoch_millis])
    val createdAt: Instant? = null,

    @Field(type = FieldType.Date, format = [DateFormat.epoch_millis])
    val updatedAt: Instant? = null,
)
