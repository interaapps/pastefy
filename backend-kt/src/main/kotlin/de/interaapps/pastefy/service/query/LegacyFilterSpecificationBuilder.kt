package de.interaapps.pastefy.service.query

import de.interaapps.pastefy.entities.TagListing
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteVisibility
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.Expression
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.sql.Timestamp
import java.time.Instant

@Component
class LegacyFilterSpecificationBuilder(
    private val parser: LegacyPasteQueryParser,
) {
    fun <T> fromRequest(
        request: HttpServletRequest,
        fields: Map<String, Field<T>>,
    ): Specification<T> {
        val filter = parser.parseClientFilter(request) ?: return Specification { _, _, builder -> builder.conjunction() }
        return Specification { root, _, builder -> predicate(filter, root, builder, fields) }
    }

    private fun <T> predicate(
        filter: LegacyFilter,
        root: Root<T>,
        builder: CriteriaBuilder,
        fields: Map<String, Field<T>>,
    ): Predicate = when (filter) {
        is LegacyFilterGroup -> {
            val children = filter.children.map { predicate(it, root, builder, fields) }.toTypedArray()
            when (filter.operator) {
                LegacyGroupOperator.AND -> if (children.isEmpty()) builder.conjunction() else builder.and(*children)
                LegacyGroupOperator.OR -> if (children.isEmpty()) builder.conjunction() else builder.or(*children)
            }
        }

        is LegacyFieldFilter -> fieldPredicate(filter, root, builder, fields)
    }

    private fun <T> fieldPredicate(
        filter: LegacyFieldFilter,
        root: Root<T>,
        builder: CriteriaBuilder,
        fields: Map<String, Field<T>>,
    ): Predicate {
        val field = fields[filter.field] ?: return builder.conjunction()
        val path = root.get<Any>(field.name)
        return when (filter.operator) {
            LegacyFilterOperator.EQ -> filter.value?.let { builder.equal(path, field.convert(it)) } ?: builder.isNull(path)
            LegacyFilterOperator.NE -> filter.value?.let { builder.notEqual(path, field.convert(it)) } ?: builder.isNotNull(path)
            LegacyFilterOperator.NULL -> builder.isNull(path)
            LegacyFilterOperator.NOT_NULL -> builder.isNotNull(path)
            LegacyFilterOperator.GT -> compare(path, field.convert(filter.value), builder) { expression, value ->
                builder.greaterThan(expression, value)
            }
            LegacyFilterOperator.GTE -> compare(path, field.convert(filter.value), builder) { expression, value ->
                builder.greaterThanOrEqualTo(expression, value)
            }
            LegacyFilterOperator.LT -> compare(path, field.convert(filter.value), builder) { expression, value ->
                builder.lessThan(expression, value)
            }
            LegacyFilterOperator.LTE -> compare(path, field.convert(filter.value), builder) { expression, value ->
                builder.lessThanOrEqualTo(expression, value)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun compare(
        path: Expression<Any>,
        value: Any?,
        builder: CriteriaBuilder,
        operation: (Expression<Comparable<Any>>, Comparable<Any>) -> Predicate,
    ): Predicate {
        if (value !is Comparable<*>) return builder.disjunction()
        return operation(path as Expression<Comparable<Any>>, value as Comparable<Any>)
    }

    data class Field<T>(
        val name: String,
        val convert: (String?) -> Any? = { it },
    )

    companion object {
        val USER_FIELDS = mapOf(
            field<User>("id"),
            field("name"),
            field("uniqueName"),
            field("email"),
            field("avatar"),
            field("authId"),
            field<User>("authProvider") { value -> value?.let { User.AuthenticationProvider.valueOf(it.uppercase()) } },
            field<User>("type") { value -> value?.let { User.Type.valueOf(it.uppercase()) } },
            field<User>("createdAt", ::instant),
            field<User>("updatedAt", ::instant),
            alias<User>("unique_name", "uniqueName"),
            alias<User>("auth_id", "authId"),
            alias<User>("auth_provider", "authProvider") { value ->
                value?.let { User.AuthenticationProvider.valueOf(it.uppercase()) }
            },
            alias<User>("created_at", "createdAt", ::instant),
            alias<User>("updated_at", "updatedAt", ::instant),
            alias<User>("eMail", "email"),
        )

        val FOLDER_FIELDS = mapOf(
            field<de.interaapps.pastefy.entities.Folder>("id", ::int),
            field("key"),
            field("name"),
            field("userId"),
            field("parent"),
            field<de.interaapps.pastefy.entities.Folder>("createdAt", ::instant),
            field<de.interaapps.pastefy.entities.Folder>("updatedAt", ::instant),
            alias<de.interaapps.pastefy.entities.Folder>("user_id", "userId"),
            alias<de.interaapps.pastefy.entities.Folder>("created_at", "createdAt", ::instant),
            alias<de.interaapps.pastefy.entities.Folder>("updated_at", "updatedAt", ::instant),
        )

        val TAG_LISTING_FIELDS = mapOf(
            field<TagListing>("tag"),
            field("displayName"),
            field("imageUrl"),
            field("description"),
            field("website"),
            field("icon"),
            field<TagListing>("pasteCount", ::int),
            alias<TagListing>("display_name", "displayName"),
            alias<TagListing>("image_url", "imageUrl"),
            alias<TagListing>("paste_count", "pasteCount", ::int),
        )

        private fun <T> field(
            name: String,
            convert: (String?) -> Any? = { it },
        ): Pair<String, Field<T>> = name to Field(name, convert)

        private fun <T> alias(
            source: String,
            target: String,
            convert: (String?) -> Any? = { it },
        ): Pair<String, Field<T>> = source to Field(target, convert)

        private fun int(value: String?): Any? = value?.toIntOrNull() ?: value

        private fun instant(value: String?): Any? =
            value?.let {
                runCatching { Instant.parse(value) }
                    .recoverCatching { Timestamp.valueOf(value).toInstant() }
                    .getOrElse { _ -> value }
            }
    }
}
