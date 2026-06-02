package de.interaapps.pastefy.entities

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "pastefy_background_jobs",
    indexes = [
        Index(name = "pastefy_background_jobs_status_available_at_index", columnList = "status, available_at"),
        Index(name = "pastefy_background_jobs_status_lease_until_index", columnList = "status, lease_until"),
        Index(name = "pastefy_background_jobs_type_entity_id_index", columnList = "type, entity_id"),
    ],
)
class BackgroundJob(
    @Id @Column(name = "`key`", length = 255) var key: String = "",
    @Enumerated(EnumType.STRING) @Column(nullable = false) var type: Type = Type.PASTE_AI_INFO,
    @Column(nullable = false) var entityId: Int = 0,
    @Column(nullable = false) var sourceVersion: Int = 0,
    @Column(nullable = false) var promptVersion: Int = 0,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: Status = Status.PENDING,
    @Column(nullable = false) var attempts: Int = 0,
    var availableAt: Instant? = null,
    var leaseUntil: Instant? = null,
    @Column(length = 64) var leaseToken: String? = null,
    @Column(length = 2048) var lastError: String? = null,
    @Column(nullable = false, updatable = false) var createdAt: Instant? = null,
    @Column(nullable = false) var updatedAt: Instant? = null,
) {
    @PrePersist fun prePersist() {
        val now = Instant.now()
        if (createdAt == null) createdAt = now
        updatedAt = now
    }
    @PreUpdate fun preUpdate() { updatedAt = Instant.now() }

    enum class Type { PASTE_AI_INFO }
    enum class Status { PENDING, RUNNING, DONE, FAILED }
}
