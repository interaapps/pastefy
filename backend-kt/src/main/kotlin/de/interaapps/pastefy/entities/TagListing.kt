package de.interaapps.pastefy.entities

import jakarta.persistence.*

@Entity
@Table(name = "pastefy_tag_listing")
class TagListing(
    @Id @Column(length = 30) var tag: String = "",
    @Column(columnDefinition = "TEXT") var displayName: String? = null,
    @Column(columnDefinition = "TEXT") var imageUrl: String? = null,
    @Column(columnDefinition = "TEXT") var description: String? = null,
    @Column(columnDefinition = "TEXT") var website: String? = null,
    @Column(columnDefinition = "TEXT") var icon: String? = null,
    @Column(nullable = true) var pasteCount: Int = 0,
) {
    @PrePersist
    @PreUpdate
    fun truncateTag() {
        if (tag.length > 30) tag = tag.substring(0, 30)
    }
}
