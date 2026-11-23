package de.interaapps.pastefy.service.query

import com.fasterxml.jackson.databind.ObjectMapper
import de.interaapps.pastefy.config.PastefyProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class LegacyPasteQueryParserTest {
    private val parser = LegacyPasteQueryParser(ObjectMapper(), PastefyProperties(paginationPageLimit = 25))

    @Test
    fun `parses legacy bracket filters and range operators`() {
        val request = MockHttpServletRequest().apply {
            addParameter("filter[visibility]", "PUBLIC")
            addParameter("filter[createdAt][\$gt]", "2026-01-01T00:00:00Z")
            addParameter("page_limit", "100")
            addParameter("sort", "+title,createdAt")
        }
        val response = MockHttpServletResponse()

        val query = parser.parse(request, response, user = null, guarded = false)
        val fields = query.filter.collectFields()

        assertEquals(25, query.pageLimit)
        assertEquals("25", response.getHeader("PAGINATION_LIMIT"))
        assertEquals(listOf(LegacySort("title", true), LegacySort("createdAt", false)), query.sorts)
        assertTrue(fields.contains(LegacyFieldFilter("visibility", LegacyFilterOperator.EQ, "PUBLIC")))
        assertTrue(fields.contains(LegacyFieldFilter("createdAt", LegacyFilterOperator.GT, "2026-01-01T00:00:00Z")))
    }

    @Test
    fun `parses filters json with or groups`() {
        val request = MockHttpServletRequest().apply {
            addParameter("filters", """{"${'$'}or":[{"visibility":"PUBLIC"},{"userId":"user-001"}]}""")
        }

        val query = parser.parse(request, MockHttpServletResponse(), user = null, guarded = false)
        val group = query.filter as LegacyFilterGroup

        assertEquals(LegacyGroupOperator.OR, group.operator)
        assertEquals(2, group.children.size)
    }

    private fun LegacyFilter?.collectFields(): List<LegacyFieldFilter> = when (this) {
        null -> emptyList()
        is LegacyFieldFilter -> listOf(this)
        is LegacyFilterGroup -> children.flatMap { it.collectFields() }
    }
}
