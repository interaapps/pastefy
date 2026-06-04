package de.interaapps.pastefy.service.query

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteVisibility
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class LegacyPasteQueryParser(
    private val objectMapper: ObjectMapper,
    private val properties: PastefyProperties,
) {
    fun parse(
        request: HttpServletRequest,
        response: HttpServletResponse,
        user: User?,
        guarded: Boolean = true,
        visibility: PasteVisibility? = null,
        encrypted: Boolean? = null,
        userId: String? = null,
        starredBy: String? = null,
        defaultSort: String = "createdAt",
        additionalFilters: List<LegacyFilter> = emptyList(),
    ): LegacyPasteQuery {
        val page = request.getParameter("page")?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val pageLimit = request.getParameter("page_limit")?.toIntOrNull()?.coerceAtLeast(1)
            ?.coerceAtMost(properties.paginationPageLimit.coerceAtLeast(1))
            ?: properties.paginationPageLimit.coerceAtLeast(1)

        response.setHeader("PAGINATION_LIMIT", pageLimit.toString())
        response.setHeader("PAGINATION_PAGE", (page - 1).toString())

        val filters = mutableListOf<LegacyFilter>()

        parseClientFilter(request)?.let(filters::add)

        request.getParameter("folder")?.takeIf(String::isNotBlank)
            ?.let { filters += LegacyFieldFilter("folder", LegacyFilterOperator.EQ, it) }

        visibility?.let { filters += LegacyFieldFilter("visibility", LegacyFilterOperator.EQ, it.name) }
        encrypted?.let { filters += LegacyFieldFilter("encrypted", LegacyFilterOperator.EQ, it.toString()) }
        userId?.let { filters += LegacyFieldFilter("userId", LegacyFilterOperator.EQ, it) }
        starredBy?.let { filters += LegacyFieldFilter("starredBy", LegacyFilterOperator.EQ, it) }
        filters += additionalFilters

        if (guarded && user?.isAdmin != true) {
            val visible = mutableListOf<LegacyFilter>(LegacyFieldFilter("visibility", LegacyFilterOperator.EQ, PasteVisibility.PUBLIC.name))
            user?.let {
                visible += LegacyFieldFilter("userId", LegacyFilterOperator.EQ, it.id)
                visible += LegacyFieldFilter("starredBy", LegacyFilterOperator.EQ, it.id)
            }
            filters += LegacyFilterGroup(LegacyGroupOperator.OR, visible)
        }

        return LegacyPasteQuery(
            page = page,
            pageLimit = pageLimit,
            search = request.getParameter("search")?.trim()?.takeIf(String::isNotEmpty),
            sorts = parseSorts(request.getParameter("sort"), defaultSort),
            filter = filters.compactAnd(),
            filterTags = request.getParameter("filter_tags")
                ?.split(',')
                ?.map(String::trim)
                ?.filter(String::isNotEmpty)
                .orEmpty(),
            shortenContent = request.getParameter("shorten_content").equals("true", true),
            withAiInfo = request.getParameter("with_ai_analysis").equals("true", true),
            currentUser = user,
        )
    }

    private fun parseSorts(raw: String?, defaultSort: String): List<LegacySort> {
        val source = raw?.takeIf(String::isNotBlank) ?: defaultSort
        return source.split(',').mapNotNull { part ->
            val value = part.trim().takeIf(String::isNotEmpty) ?: return@mapNotNull null
            LegacySort(field = value.removePrefix("+"), ascending = value.startsWith("+"))
        }
    }

    private fun parseClientFilter(request: HttpServletRequest): LegacyFilter? {
        request.getParameter("filters")?.trim()?.takeIf(String::isNotEmpty)?.let {
            return parseJsonObject(objectMapper.readTree(it))
        }

        request.getParameter("filter")?.trim()?.takeIf { it.startsWith("{") }?.let {
            return LegacyFilterGroup(LegacyGroupOperator.AND, listOf(parseJsonObject(objectMapper.readTree(it))))
        }

        val formFilter = mutableMapOf<String, Any?>()

        request.parameterMap.forEach { (name, values) ->
            val tokens = BRACKET_TOKEN.findAll(name).map { it.groupValues[1] }.toList()
            if (tokens.firstOrNull() == "filter" && tokens.size > 1) {
                insert(formFilter, tokens.drop(1), values.map(String::trim).filter(String::isNotEmpty))
            }
        }

        if (formFilter.isEmpty()) return null

        return LegacyFilterGroup(LegacyGroupOperator.AND, listOf(parseObject(formFilter)))
    }

    private fun insert(target: MutableMap<String, Any?>, path: List<String>, values: List<String>) {
        if (path.isEmpty()) return
        val key = path.first()
        if (path.size == 1) {
            target[key] = values.firstOrNull().orEmpty()
            return
        }
        val child = target.getOrPut(key) { mutableMapOf<String, Any?>() }
        if (child is MutableMap<*, *>) {
            @Suppress("UNCHECKED_CAST")
            insert(child as MutableMap<String, Any?>, path.drop(1), values)
        }
    }

    private fun parseJsonObject(node: JsonNode): LegacyFilter = parseObject(jsonToAny(node) as Map<String, Any?>)

    private fun jsonToAny(node: JsonNode): Any? = when {
        node.isObject -> node.fields().asSequence().associate { it.key to jsonToAny(it.value) }
        node.isArray -> node.map(::jsonToAny)
        node.isNull -> null
        node.isBoolean -> node.booleanValue().toString()
        node.isNumber -> node.asText()
        else -> node.asText()
    }

    private fun parseObject(values: Map<String, Any?>): LegacyFilter =
        values.mapNotNull { (key, value) -> parseEntry(key, value) }.compactAnd()
            ?: LegacyFilterGroup(LegacyGroupOperator.AND, emptyList())

    private fun parseEntry(key: String, value: Any?): LegacyFilter? = when (key) {
        "\$and" -> LegacyFilterGroup(LegacyGroupOperator.AND, parseGroupChildren(value))
        "\$or" -> LegacyFilterGroup(LegacyGroupOperator.OR, parseGroupChildren(value))
        else -> parseField(key, value)
    }

    private fun parseGroupChildren(value: Any?): List<LegacyFilter> = when (value) {
        is List<*> -> value.mapNotNull { parseGroupChild(it) }
        is Map<*, *> -> listOfNotNull(parseGroupChild(value))
        else -> emptyList()
    }

    private fun parseGroupChild(value: Any?): LegacyFilter? = when (value) {
        is Map<*, *> -> {
            @Suppress("UNCHECKED_CAST")
            parseObject(value as Map<String, Any?>)
        }
        else -> null
    }

    private fun parseField(field: String, value: Any?): LegacyFilter? {
        if (value is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            val operators = value as Map<String, Any?>
            val filters = operators.mapNotNull { (operator, operatorValue) ->
                LegacyFilterOperator.fromLegacyName(operator)
                    ?.let { LegacyFieldFilter(field, it, operatorValue?.toString()) }
            }
            if (filters.isNotEmpty()) return filters.compactAnd()
        }
        return LegacyFieldFilter(field, LegacyFilterOperator.EQ, value?.toString())
    }

    private fun List<LegacyFilter>.compactAnd(): LegacyFilter? = when (size) {
        0 -> null
        1 -> first()
        else -> LegacyFilterGroup(LegacyGroupOperator.AND, this)
    }

    companion object {
        private val BRACKET_TOKEN = Regex("([^\\[\\]]+)")
    }
}
