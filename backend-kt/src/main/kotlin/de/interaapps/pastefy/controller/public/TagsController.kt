package de.interaapps.pastefy.controller.public

import de.interaapps.pastefy.auth.annotations.PublicPastesEnabled
import de.interaapps.pastefy.entities.TagListing
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import de.interaapps.pastefy.repositories.TagListingRepository
import de.interaapps.pastefy.service.TagListingService
import de.interaapps.pastefy.service.query.LegacyFilterSpecificationBuilder
import de.interaapps.pastefy.service.query.LegacyFilterSpecificationBuilder.Companion.TAG_LISTING_FIELDS
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v2/public/tags")
@PublicPastesEnabled
class TagsController(
    private val repository: TagListingRepository,
    private val tags: TagListingService,
    private val legacyFilters: LegacyFilterSpecificationBuilder,
) {
    @GetMapping
    fun getTags(
        request: HttpServletRequest,
        @RequestParam("search") search: String? = null,
        @RequestParam("page", defaultValue = "1") page: Int,
        @RequestParam("page_limit", defaultValue = "10") pageLimit: Int,
    ): List<TagListing> {
        val pageable = PageRequest.of(
            page.coerceAtLeast(1) - 1,
            pageLimit.coerceIn(1, 100),
            Sort.by(Sort.Direction.DESC, "pasteCount"),
        )
        val searchSpecification = Specification<TagListing> { root, _, builder ->
            search?.trim()?.takeIf(String::isNotEmpty)?.lowercase()?.let {
                builder.or(
                    builder.like(builder.lower(root.get("tag")), "%$it%"),
                    builder.like(builder.lower(root.get("displayName")), "%$it%"),
                    builder.like(builder.lower(root.get("description")), "%$it%"),
                )
            } ?: builder.conjunction()
        }
        return repository.findAll(
            searchSpecification.and(legacyFilters.fromRequest(request, TAG_LISTING_FIELDS)),
            pageable,
        ).content
    }

    @GetMapping("/{tag}")
    @Cacheable("public-tag", key = "#tag")
    fun getTag(@PathVariable tag: String): TagListing = tags.getOrCreate(tag)
}
