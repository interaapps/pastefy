package de.interaapps.pastefy.service

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.dto.pastes.PasteResponse
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.PasteStar
import de.interaapps.pastefy.entities.PasteTag
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.exceptions.PermissionsDeniedException
import de.interaapps.pastefy.repositories.PasteRepository
import jakarta.persistence.criteria.Predicate
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasteQueryService(
    private val pasteRepository: PasteRepository,
    private val mapper: PasteResponseMapper,
    private val properties: PastefyProperties,
) {
    fun list(
        request: HttpServletRequest,
        response: HttpServletResponse,
        user: User?,
        guarded: Boolean = true,
        visibility: PasteVisibility? = null,
        encrypted: Boolean? = null,
        userId: String? = null,
        starredBy: String? = null,
    ): List<PasteResponse> {
        val paging = paging(request, response)
        val specification = specification(request, user, guarded, visibility, encrypted, userId, starredBy)
        val sort = Sort.by(Sort.Direction.DESC, sortable(request.getParameter("sort")))
        return pasteRepository.findAll(specification, PageRequest.of(paging.page - 1, paging.limit, sort)).content
            .map {
                mapper.map(
                    it,
                    user,
                    fetchStar = user != null,
                    fetchUser = true,
                    request.withAiInfo(),
                    request.shortenContent()
                )
            }
    }

    fun trending(request: HttpServletRequest, response: HttpServletResponse): List<PasteResponse> {
        val paging = paging(request, response)
        val createdAfter =
            if (request.parameterMap.containsKey("trending")) Instant.now().minus(4, ChronoUnit.DAYS) else null
        return pasteRepository.findTrending(createdAfter, PageRequest.of(paging.page - 1, paging.limit))
            .map { mapper.map(it, shortenContent = request.shortenContent(), withAiInfo = request.withAiInfo()) }
    }

    fun map(paste: Paste, request: HttpServletRequest, user: User?) =
        mapper.map(paste, user, fetchStar = true, fetchUser = true, request.withAiInfo(), request.shortenContent())

    private fun specification(
        request: HttpServletRequest,
        user: User?,
        guarded: Boolean,
        visibility: PasteVisibility?,
        encrypted: Boolean?,
        userId: String?,
        starredBy: String?,
    ) = Specification<Paste> { root, query, builder ->
        val criteriaQuery = requireNotNull(query)
        val predicates = mutableListOf<Predicate>()
        visibility?.let { predicates += builder.equal(root.get<PasteVisibility>("visibility"), it) }
        encrypted?.let { predicates += builder.equal(root.get<Boolean>("encrypted"), it) }
        userId?.let { predicates += builder.equal(root.get<String>("userId"), it) }
        request.getParameter("folder")?.let { predicates += builder.equal(root.get<String>("folder"), it) }
        request.getParameter("search")?.trim()?.takeIf(String::isNotEmpty)?.lowercase()?.let {
            predicates += builder.or(
                builder.like(builder.lower(root.get("title")), "%$it%"),
                builder.like(builder.lower(root.get("content")), "%$it%"),
            )
        }
        request.getParameter("filter_tags")?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.forEach { tag ->
            val subquery = criteriaQuery.subquery(String::class.java)
            val pasteTag = subquery.from(PasteTag::class.java)
            subquery.select(pasteTag.get("paste")).where(builder.equal(pasteTag.get<String>("tag"), tag))
            predicates += root.get<String>("key").`in`(subquery)
        }
        starredBy?.let { predicates += starredPredicate(criteriaQuery, builder, root.get("key"), it) }
        if (guarded && user?.isAdmin != true) {
            val visible =
                mutableListOf<Predicate>(builder.equal(root.get<PasteVisibility>("visibility"), PasteVisibility.PUBLIC))
            user?.let {
                visible += builder.equal(root.get<String>("userId"), it.id)
                visible += starredPredicate(criteriaQuery, builder, root.get("key"), it.id)
            }
            predicates += builder.or(*visible.toTypedArray())
        }
        builder.and(*predicates.toTypedArray())
    }

    private fun starredPredicate(
        query: jakarta.persistence.criteria.CriteriaQuery<*>,
        builder: jakarta.persistence.criteria.CriteriaBuilder,
        pasteKey: jakarta.persistence.criteria.Path<String>,
        userId: String,
    ): Predicate {
        val subquery = query.subquery(String::class.java)
        val star = subquery.from(PasteStar::class.java)
        subquery.select(star.get("paste")).where(builder.equal(star.get<String>("userId"), userId))
        return pasteKey.`in`(subquery)
    }

    private fun paging(request: HttpServletRequest, response: HttpServletResponse): Paging {
        val page = request.getParameter("page")?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val limit = request.getParameter("page_limit")?.toIntOrNull()?.coerceAtLeast(1)
            ?.coerceAtMost(properties.paginationPageLimit.coerceAtLeast(1))
            ?: properties.paginationPageLimit.coerceAtLeast(1)
        response.setHeader("PAGINATION_LIMIT", limit.toString())
        response.setHeader("PAGINATION_PAGE", (page - 1).toString())
        return Paging(page, limit)
    }

    private fun sortable(value: String?) = when (value) {
        "updatedAt", "updated_at" -> "updatedAt"
        "title" -> "title"
        else -> "createdAt"
    }

    private fun HttpServletRequest.withAiInfo() = getParameter("with_ai_analysis").equals("true", true)
    private fun HttpServletRequest.shortenContent() = getParameter("shorten_content").equals("true", true)
    private data class Paging(val page: Int, val limit: Int)
}
