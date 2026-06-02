package de.interaapps.pastefy.entities

import jakarta.persistence.*

@Entity
@Table(
    name = "pastefy_paste_tags",
    indexes = [
        Index(name = "pastefy_paste_tags_paste_tag_index", columnList = "paste, tag"),
    ],
)
class PasteTag(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Int? = null,
    @Column(length = 8, nullable = false) var paste: String = "",
    @Column(length = 30, nullable = false) var tag: String = "",
) {
    @PrePersist
    @PreUpdate
    fun truncateTag() {
        if (tag.length > 30) tag = tag.substring(0, 30)
    }
}
