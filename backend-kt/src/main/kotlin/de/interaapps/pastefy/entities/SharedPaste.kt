package de.interaapps.pastefy.entities

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "pastefy_shared_pastes",
    indexes = [
        Index(name = "pastefy_shared_pastes_target_id_index", columnList = "target_id"),
        Index(name = "pastefy_shared_pastes_user_id_index", columnList = "user_id"),
    ],
)
class SharedPaste(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int? = null,
    @Column(length = 8, nullable = true) var userId: String = "",
    @Column(length = 8, nullable = true) var targetId: String = "",
    @Column(columnDefinition = "TEXT", nullable = true) var paste: String = "",
    @Column(nullable = false, updatable = false) var createdAt: Instant? = null,
    @Column(nullable = false) var updatedAt: Instant? = null,
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
