package de.interaapps.pastefy.service

import de.interaapps.pastefy.entities.TagListing
import de.interaapps.pastefy.repositories.PasteTagRepository
import de.interaapps.pastefy.repositories.TagListingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TagListingService(
    private val tagListingRepository: TagListingRepository,
    private val pasteTagRepository: PasteTagRepository,
) {
    @Transactional
    fun getOrCreate(rawTag: String): TagListing {
        val tag = rawTag.take(30)
        return tagListingRepository.findById(tag).orElseGet {
            tagListingRepository.save(TagListing(tag = tag))
        }
    }

    @Transactional
    fun updateCount(rawTag: String): TagListing {
        val listing = getOrCreate(rawTag)
        listing.pasteCount = pasteTagRepository.countByTag(listing.tag)
        return tagListingRepository.save(listing)
    }
}
