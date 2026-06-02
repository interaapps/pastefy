package de.interaapps.pastefy.entities

import jakarta.persistence.*

@Entity
@Table(name = "pastefy_tag_listing")
class TagListing(
    @Id @Column(length = 30) var tag: String = "",
    var displayName: String? = null,
    var imageUrl: String? = null,
    var description: String? = null,
    var website: String? = null,
    var icon: String? = null,
    @Column(nullable = false) var pasteCount: Int = 0,
) {
    @PrePersist
    @PreUpdate
    fun truncateTag() {
        if (tag.length > 30) tag = tag.substring(0, 30)
    }
}
