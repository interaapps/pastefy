package de.interaapps.pastefy.entities

import de.interaapps.pastefy.util.RandomStrings
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "pastefy_folder",
    indexes = [
        Index(name = "pastefy_folder_key_index", columnList = "`key`"),
        Index(name = "pastefy_folder_user_id_index", columnList = "user_id"),
    ],
)
class Folder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "`key`", length = 8, nullable = false, unique = true)
    var key: String = RandomStrings.alphanumeric(8),

    @Column(nullable = false)
    var name: String = "",

    @Column(length = 8, nullable = false)
    var userId: String = "",

    @Column(length = 8)
    var parent: String? = null,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(nullable = false)
    var updatedAt: Instant? = null,
) {
    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        if (createdAt == null) createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}
