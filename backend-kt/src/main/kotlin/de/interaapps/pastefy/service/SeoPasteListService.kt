package de.interaapps.pastefy.service

import de.interaapps.pastefy.dto.pastes.PasteResponse
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.service.query.LegacyFieldFilter
import de.interaapps.pastefy.service.query.LegacyFilter
import de.interaapps.pastefy.service.query.LegacyFilterGroup
import de.interaapps.pastefy.service.query.LegacyFilterOperator
import de.interaapps.pastefy.service.query.LegacyGroupOperator
import de.interaapps.pastefy.service.query.LegacyPasteQuery
import de.interaapps.pastefy.service.query.LegacySort
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class SeoPasteListService(
    private val queries: PasteQueryService,
) {
    fun latestPublic(limit: Int): List<PasteResponse> =
        queries.search(publicQuery(limit = limit, sort = "createdAt"))

    fun recentTrending(limit: Int): List<PasteResponse> =
        queries.search(
            publicQuery(
                limit = limit,
                sort = "engagementScore",
                additionalFilters = listOf(
                    LegacyFieldFilter(
                        field = "createdAt",
                        operator = LegacyFilterOperator.GT,
                        value = Instant.now().minus(4, ChronoUnit.DAYS).toString(),
                    ),
                ),
            ),
        )

    fun allTimeTrending(limit: Int): List<PasteResponse> =
        queries.search(publicQuery(limit = limit, sort = "engagementScore"))

    fun publicByUser(userId: String, limit: Int): List<PasteResponse> =
        queries.search(
            publicQuery(
                limit = limit,
                sort = "createdAt",
                additionalFilters = listOf(LegacyFieldFilter("userId", LegacyFilterOperator.EQ, userId)),
            ),
        )

    private fun publicQuery(
        limit: Int,
        sort: String,
        additionalFilters: List<LegacyFilter> = emptyList(),
    ): LegacyPasteQuery =
        LegacyPasteQuery(
            page = 1,
            pageLimit = limit.coerceAtLeast(1),
            search = null,
            sorts = listOf(LegacySort(field = sort, ascending = false)),
            filter = LegacyFilterGroup(
                LegacyGroupOperator.AND,
                listOf(
                    LegacyFieldFilter("visibility", LegacyFilterOperator.EQ, PasteVisibility.PUBLIC.name),
                    LegacyFieldFilter("encrypted", LegacyFilterOperator.EQ, false.toString()),
                ) + additionalFilters,
            ),
            filterTags = emptyList(),
            shortenContent = true,
            withAiInfo = true,
            currentUser = null,
        )
}
