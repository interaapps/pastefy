package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.TagListing
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface TagListingRepository : JpaRepository<TagListing, String>, JpaSpecificationExecutor<TagListing> {
    fun findAllByOrderByPasteCountDesc(pageable: Pageable): List<TagListing>
    fun findAllByTagContainingIgnoreCaseOrDisplayNameContainingIgnoreCaseOrderByPasteCountDesc(
        tag: String,
        displayName: String,
        pageable: Pageable,
    ): List<TagListing>
}
