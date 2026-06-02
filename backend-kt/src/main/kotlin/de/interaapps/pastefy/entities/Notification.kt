package de.interaapps.pastefy.entities

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "pastefy_notification",
    indexes = [
        Index(name = "pastefy_notification_user_id_received_index", columnList = "user_id, received"),
    ],
)
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    var message: String? = null,
    @Column(length = 8, nullable = false) var userId: String = "",
    var url: String? = null,
    @Column(nullable = false) var alreadyRead: Boolean = false,
    @Column(nullable = false) var received: Boolean = false,
    @Column(nullable = false, updatable = false) var createdAt: Instant? = null,
    @Column(nullable = false) var updatedAt: Instant? = null,
) {
    @PrePersist fun prePersist() {
        val now = Instant.now()
        if (createdAt == null) createdAt = now
        updatedAt = now
    }
    @PreUpdate fun preUpdate() { updatedAt = Instant.now() }
}
