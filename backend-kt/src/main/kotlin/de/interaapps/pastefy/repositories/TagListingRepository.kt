package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.TagListing
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface TagListingRepository : JpaRepository<TagListing, String> {
    fun findAllByOrderByPasteCountDesc(pageable: Pageable): List<TagListing>
    fun findAllByTagContainingIgnoreCaseOrDisplayNameContainingIgnoreCaseOrderByPasteCountDesc(
        tag: String,
        displayName: String,
        pageable: Pageable,
    ): List<TagListing>
}
