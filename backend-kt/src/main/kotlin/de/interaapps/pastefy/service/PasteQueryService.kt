package de.interaapps.pastefy.service

import de.interaapps.pastefy.dto.pastes.PasteResponse
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.infrastructure.elastic.ElasticPasteQueryAdapter
import de.interaapps.pastefy.service.query.JpaPasteQueryAdapter
import de.interaapps.pastefy.service.query.LegacyFieldFilter
import de.interaapps.pastefy.service.query.LegacyFilterOperator
import de.interaapps.pastefy.service.query.LegacyPasteQuery
import de.interaapps.pastefy.service.query.LegacyPasteQueryParser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasteQueryService(
    private val parser: LegacyPasteQueryParser,
    private val jpa: JpaPasteQueryAdapter,
    private val mapper: PasteResponseMapper,
    private val pasteMetricsService: PasteMetricsService,
    private val elasticProvider: ObjectProvider<ElasticPasteQueryAdapter>,
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
        val query = parser.parse(
            request = request,
            response = response,
            user = user,
            guarded = guarded,
            visibility = visibility,
            encrypted = encrypted,
            userId = userId,
            starredBy = starredBy,
        )
        elasticProvider.ifAvailable {
            response.addHeader("using-elastic", "true")
        }
        return search(query, currentUser = user, fetchStar = user != null, fetchUser = true)
    }

    fun trending(request: HttpServletRequest, response: HttpServletResponse): List<PasteResponse> {
        val additionalFilters = buildList {
            if (request.parameterMap.containsKey("trending")) {
                add(
                    LegacyFieldFilter(
                        field = "createdAt",
                        operator = LegacyFilterOperator.GT,
                        value = Instant.now().minus(4, ChronoUnit.DAYS).toString(),
                    ),
                )
            }
        }
        val query = parser.parse(
            request = request,
            response = response,
            user = null,
            guarded = false,
            visibility = PasteVisibility.PUBLIC,
            encrypted = false,
            defaultSort = "engagementScore",
            additionalFilters = additionalFilters,
        )
        elasticProvider.ifAvailable {
            response.addHeader("using-elastic", "true")
        }
        return search(query, fetchUser = true)
    }

    fun search(
        query: LegacyPasteQuery,
        currentUser: User? = query.currentUser,
        fetchStar: Boolean = currentUser != null,
        fetchUser: Boolean = true,
    ): List<PasteResponse> =
        elasticProvider.ifAvailable?.find(query)
            ?: jpa.find(query).let { pastes ->
                val metrics = pasteMetricsService.getMetrics(pastes.map { it.key })
                pastes.map {
                    mapper.map(
                        it,
                        currentUser,
                        fetchStar = fetchStar,
                        fetchUser = fetchUser,
                        shortenContent = query.shortenContent,
                        withAiInfo = query.withAiInfo,
                        metrics = metrics[it.key],
                    )
                }
            }

    fun map(paste: Paste, request: HttpServletRequest, user: User?) =
        mapper.map(
            paste,
            user,
            fetchStar = true,
            fetchUser = true,
            withAiInfo = request.getParameter("with_ai_analysis").equals("true", true),
            shortenContent = request.getParameter("shorten_content").equals("true", true),
        )
}
