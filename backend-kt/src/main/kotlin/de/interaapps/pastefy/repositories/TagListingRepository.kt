package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.TagListing
import org.springframework.data.jpa.repository.JpaRepository

interface TagListingRepository : JpaRepository<TagListing, String>
