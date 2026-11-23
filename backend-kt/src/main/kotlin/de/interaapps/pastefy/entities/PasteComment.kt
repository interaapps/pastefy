package de.interaapps.pastefy.entities

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "pastefy_paste_comments",
    indexes = [
        Index(name = "pastefy_paste_comments_parent_id_index", columnList = "parent_id"),
        Index(name = "pastefy_paste_comments_paste_index", columnList = "paste"),
        Index(name = "pastefy_paste_comments_paste_line_index", columnList = "paste, line_from"),
        Index(name = "pastefy_paste_comments_user_id_index", columnList = "user_id"),
    ],
)
class PasteComment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int? = null,
    @Column(length = 8, nullable = false) var paste: String = "",
    @Column(length = 8, nullable = false) var userId: String = "",
    @Column(length = 2000, nullable = false) var content: String = "",
    var parentId: Int? = null,
    var lineFrom: Int? = null,
    var lineTo: Int? = null,
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
