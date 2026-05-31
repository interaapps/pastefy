package de.interaapps.pastefy.model.database

import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.enums.StorageType
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "pastes")
class Paste(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(length = 8, nullable = false, unique = true)
    var key: String = randomKey(),

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
    var indexedInElastic: Boolean = false

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
            key = randomKey()
        }
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
        version += 1
        indexedInElastic = false
    }


    companion object {
        fun randomKey(): String =
            UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
    }
}