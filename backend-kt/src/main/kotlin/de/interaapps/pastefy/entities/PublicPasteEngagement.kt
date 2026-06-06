package de.interaapps.pastefy.entities

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "pastefy_public_paste_engagements",
    indexes = [
        Index(name = "pastefy_public_paste_engagements_id_score_index", columnList = "paste_id, score"),
        Index(name = "pastefy_public_paste_engagements_paste_id_index", columnList = "paste_id"),
    ],
)
class PublicPasteEngagement(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int? = null,
    @Column(nullable = true) var pasteId: Int = 0,
    @Column(nullable = true) var score: Int = 0,
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
