package de.interaapps.pastefy.entities

import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.enums.StorageType
import de.interaapps.pastefy.util.RandomStrings
import jakarta.persistence.*
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.HexFormat

@Entity
@Table(
    name = "pastefy_pastes",
    indexes = [
        Index(name = "pastefy_pastes_expire_at_index", columnList = "expire_at"),
        Index(name = "pastefy_pastes_folder_index", columnList = "folder"),
        Index(name = "pastefy_pastes_hash_index", columnList = "hash"),
        Index(name = "pastefy_pastes_id_index", columnList = "id"),
        Index(name = "pastefy_pastes_indexed_in_elastic_index", columnList = "indexed_in_elastic"),
        Index(name = "pastefy_pastes_key_index", columnList = "`key`"),
        Index(name = "pastefy_pastes_length_index", columnList = "length"),
        Index(name = "pastefy_pastes_storage_type_index", columnList = "storage_type"),
        Index(name = "pastefy_pastes_user_folder_index", columnList = "user_id, folder"),
        Index(name = "pastefy_pastes_user_id_index", columnList = "user_id"),
        Index(name = "pastefy_pastes_visibility_index", columnList = "visibility"),
    ],
)
class Paste(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "`key`", length = 8, nullable = false, unique = true)
    var key: String = RandomStrings.alphanumeric(8),

    @Column(length = 8)
    var folder: String? = null,

    var expireAt: Instant? = null,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(nullable = false)
    var updatedAt: Instant? = null,

    var title: String? = null,

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    var content: String? = null,

    @Column(length = 8)
    var userId: String? = null,

    @Column(length = 8)
    var forkedFrom: String? = null,

    @Column(nullable = false)
    var encrypted: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('PASTE','MULTI_PASTE')")
    var type: PasteType = PasteType.PASTE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('UNLISTED','PUBLIC','PRIVATE')")
    var visibility: PasteVisibility = PasteVisibility.UNLISTED,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('S3','DATABASE','HTTP')")
    var storageType: StorageType = StorageType.DATABASE,

    @Column(nullable = false)
    var version: Int = 0,

    @Column(nullable = false)
    var indexedInElastic: Boolean = false,

    @Column(nullable = false)
    var length: Int = 0,

    @Column(length = 64)
    var hash: String? = null,

    ) {

    @Transient
    var cachedContents: String? = null

    val rawContent: String?
        get() = content

    val isPublic: Boolean
        get() = visibility == PasteVisibility.PUBLIC

    val isPrivate: Boolean
        get() = visibility == PasteVisibility.PRIVATE

    fun setDatabaseContent(content: String?) {
        this.content = content
        this.storageType = StorageType.DATABASE
        updateContentMetadata(content)
    }

    fun setStorageReference(reference: String?, storageType: StorageType) {
        content = reference
        this.storageType = storageType
    }

    @PrePersist
    fun prePersist() {
        val now = Instant.now()

        if (createdAt == null) {
            createdAt = now
        }

        updatedAt = now
        version += 1
        indexedInElastic = false

        if (key.isBlank()) {
            key = RandomStrings.alphanumeric(8)
        }
        if (storageType == StorageType.DATABASE) updateContentMetadata(content)
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
        version += 1
        indexedInElastic = false
        if (storageType == StorageType.DATABASE) updateContentMetadata(content)
    }

    private fun updateContentMetadata(content: String?) {
        if (content == null) {
            length = 0
            hash = null
            return
        }
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        length = bytes.size
        hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))
    }
}
