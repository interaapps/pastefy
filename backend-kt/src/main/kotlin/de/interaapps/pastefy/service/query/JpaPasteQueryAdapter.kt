package de.interaapps.pastefy.service.query

import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.PasteStar
import de.interaapps.pastefy.entities.PasteTag
import de.interaapps.pastefy.entities.PublicPasteEngagement
import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.enums.StorageType
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant

@Component
class JpaPasteQueryAdapter(
    private val entityManager: EntityManager,
) {
    fun find(query: LegacyPasteQuery): List<Paste> {
        val builder = entityManager.criteriaBuilder
        val criteria = builder.createQuery(Paste::class.java)
        val root = criteria.from(Paste::class.java)

        val predicates = mutableListOf<Predicate>()
        query.filter?.let { predicates += predicate(it, root, criteria, builder) }
        query.search?.let { predicates += searchPredicate(it, root, builder) }
        if (query.filterTags.isNotEmpty()) predicates += tagsPredicate(query.filterTags, root, criteria, builder)

        if (predicates.isNotEmpty()) criteria.where(builder.and(*predicates.toTypedArray()))
        criteria.orderBy(query.sorts.map { order(it, root, criteria, builder) })

        return entityManager.createQuery(criteria)
            .setFirstResult(query.offset)
            .setMaxResults(query.pageLimit)
            .resultList
    }

    private fun predicate(
        filter: LegacyFilter,
        root: Root<Paste>,
        query: CriteriaQuery<Paste>,
        builder: CriteriaBuilder,
    ): Predicate = when (filter) {
        is LegacyFilterGroup -> {
            val children = filter.children.map { predicate(it, root, query, builder) }.toTypedArray()
            when (filter.operator) {
                LegacyGroupOperator.AND -> if (children.isEmpty()) builder.conjunction() else builder.and(*children)
                LegacyGroupOperator.OR -> if (children.isEmpty()) builder.conjunction() else builder.or(*children)
            }
        }
        is LegacyFieldFilter -> fieldPredicate(filter, root, query, builder)
    }

    private fun fieldPredicate(
        filter: LegacyFieldFilter,
        root: Root<Paste>,
        query: CriteriaQuery<Paste>,
        builder: CriteriaBuilder,
    ): Predicate {
        val field = normalizeField(filter.field)
        if (field == "starredBy") return starredPredicate(filter, root, query, builder)
        if (field == "tags") return tagsPredicate(listOfNotNull(filter.value), root, query, builder, filter.operator)
        if (field == "engagementScore") return engagementPredicate(filter, root, query, builder)

        val path = root.get<Any>(field)
        return when (filter.operator) {
            LegacyFilterOperator.EQ -> filter.value?.let { builder.equal(path, convert(field, it)) } ?: builder.isNull(path)
            LegacyFilterOperator.NE -> filter.value?.let { builder.notEqual(path, convert(field, it)) } ?: builder.isNotNull(path)
            LegacyFilterOperator.NULL -> builder.isNull(path)
            LegacyFilterOperator.NOT_NULL -> builder.isNotNull(path)
            LegacyFilterOperator.GT -> compare(field, filter.value, root, builder) { expression, value -> builder.greaterThan(expression, value) }
            LegacyFilterOperator.GTE -> compare(field, filter.value, root, builder) { expression, value -> builder.greaterThanOrEqualTo(expression, value) }
            LegacyFilterOperator.LT -> compare(field, filter.value, root, builder) { expression, value -> builder.lessThan(expression, value) }
            LegacyFilterOperator.LTE -> compare(field, filter.value, root, builder) { expression, value -> builder.lessThanOrEqualTo(expression, value) }
        }
    }

    private fun starredPredicate(
        filter: LegacyFieldFilter,
        root: Root<Paste>,
        query: CriteriaQuery<Paste>,
        builder: CriteriaBuilder,
    ): Predicate {
        val subquery = query.subquery(String::class.java)
        val star = subquery.from(PasteStar::class.java)
        subquery.select(star.get("paste")).where(builder.equal(star.get<String>("userId"), filter.value))
        val predicate = root.get<String>("key").`in`(subquery)
        return if (filter.operator == LegacyFilterOperator.NE) builder.not(predicate) else predicate
    }

    private fun tagsPredicate(
        tags: List<String>,
        root: Root<Paste>,
        query: CriteriaQuery<Paste>,
        builder: CriteriaBuilder,
        operator: LegacyFilterOperator = LegacyFilterOperator.EQ,
    ): Predicate {
        if (tags.isEmpty()) return builder.conjunction()
        val subquery = query.subquery(String::class.java)
        val pasteTag = subquery.from(PasteTag::class.java)
        subquery.select(pasteTag.get("paste")).where(pasteTag.get<String>("tag").`in`(tags))
        val predicate = root.get<String>("key").`in`(subquery)
        return if (operator == LegacyFilterOperator.NE) builder.not(predicate) else predicate
    }

    private fun engagementPredicate(
        filter: LegacyFieldFilter,
        root: Root<Paste>,
        query: CriteriaQuery<Paste>,
        builder: CriteriaBuilder,
    ): Predicate {
        val score = engagementExpression(root, query, builder)
        val value = filter.value?.toIntOrNull() ?: 0
        return when (filter.operator) {
            LegacyFilterOperator.EQ -> builder.equal(score, value)
            LegacyFilterOperator.NE -> builder.notEqual(score, value)
            LegacyFilterOperator.NULL -> builder.equal(score, 0)
            LegacyFilterOperator.NOT_NULL -> builder.notEqual(score, 0)
            LegacyFilterOperator.GT -> builder.greaterThan(score, value)
            LegacyFilterOperator.GTE -> builder.greaterThanOrEqualTo(score, value)
            LegacyFilterOperator.LT -> builder.lessThan(score, value)
            LegacyFilterOperator.LTE -> builder.lessThanOrEqualTo(score, value)
        }
    }

    private fun searchPredicate(search: String, root: Root<Paste>, builder: CriteriaBuilder): Predicate {
        val pattern = "%${search.lowercase()}%"
        return builder.or(
            builder.like(builder.lower(root.get("title")), pattern),
            builder.like(root.get("content"), "%$search%"),
        )
    }

    private fun order(
        sort: LegacySort,
        root: Root<Paste>,
        query: CriteriaQuery<Paste>,
        builder: CriteriaBuilder,
    ): jakarta.persistence.criteria.Order {
        val field = normalizeField(sort.field)
        val expression: Expression<out Comparable<*>> = if (field == "engagementScore") {
            engagementExpression(root, query, builder)
        } else {
            root.get(field)
        }
        return if (sort.ascending) builder.asc(expression) else builder.desc(expression)
    }

    private fun engagementExpression(
        root: Root<Paste>,
        query: CriteriaQuery<Paste>,
        builder: CriteriaBuilder,
    ): Expression<Int> {
        val subquery = query.subquery(Int::class.java)
        val engagement = subquery.from(PublicPasteEngagement::class.java)
        subquery.select(engagement.get("score"))
            .where(builder.equal(engagement.get<Int>("pasteId"), root.get<Int>("id")))
        return builder.coalesce(subquery, 0)
    }

    private fun compare(
        field: String,
        rawValue: String?,
        root: Root<Paste>,
        builder: CriteriaBuilder,
        operation: (Expression<Comparable<Any>>, Comparable<Any>) -> Predicate,
    ): Predicate {
        val value = rawValue?.let { convert(field, it) as? Comparable<Any> } ?: return builder.disjunction()
        @Suppress("UNCHECKED_CAST")
        val expression = root.get<Comparable<Any>>(field)
        return operation(expression, value)
    }

    private fun normalizeField(field: String): String = FIELD_ALIASES[field] ?: field

    private fun convert(field: String, value: String): Any = when (field) {
        "id", "version", "length" -> value.toIntOrNull() ?: value
        "encrypted", "indexedInElastic" -> value.toBooleanStrictOrNull() ?: value.equals("true", true)
        "visibility" -> runCatching { PasteVisibility.valueOf(value.uppercase()) }.getOrElse { value }
        "type" -> runCatching { PasteType.valueOf(value.uppercase()) }.getOrElse { value }
        "storageType" -> runCatching { StorageType.valueOf(value.uppercase()) }.getOrElse { value }
        "createdAt", "updatedAt", "expireAt" -> parseInstant(value) ?: value
        else -> value
    }

    private fun parseInstant(value: String): Instant? =
        runCatching { Instant.parse(value) }
            .recoverCatching { Timestamp.valueOf(value).toInstant() }
            .getOrNull()

    companion object {
        private val FIELD_ALIASES = mapOf(
            "created_at" to "createdAt",
            "updated_at" to "updatedAt",
            "expire_at" to "expireAt",
            "user_id" to "userId",
            "forked_from" to "forkedFrom",
            "storage_type" to "storageType",
            "indexed_in_elastic" to "indexedInElastic",
            "engagement_score" to "engagementScore",
            "starred_by" to "starredBy",
            "star_count" to "starCount",
        )
    }
}
