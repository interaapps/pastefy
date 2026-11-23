package de.interaapps.pastefy.infrastructure.elastic

import co.elastic.clients.elasticsearch._types.FieldValue
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField
import co.elastic.clients.elasticsearch._types.query_dsl.Query as ElasticQuery
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.dto.pastes.PasteResponse
import de.interaapps.pastefy.dto.user.PublicUserDto
import de.interaapps.pastefy.service.PasteMetrics
import de.interaapps.pastefy.service.PasteMetricsService
import de.interaapps.pastefy.service.query.LegacyFieldFilter
import de.interaapps.pastefy.service.query.LegacyFilter
import de.interaapps.pastefy.service.query.LegacyFilterGroup
import de.interaapps.pastefy.service.query.LegacyFilterOperator
import de.interaapps.pastefy.service.query.LegacyGroupOperator
import de.interaapps.pastefy.service.query.LegacyPasteQuery
import de.interaapps.pastefy.util.shorten
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.client.elc.NativeQuery
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "pastefy.elasticsearch", name = ["enabled"], havingValue = "true")
class ElasticPasteQueryAdapter(
    private val operations: ElasticsearchOperations,
    private val properties: PastefyProperties,
    private val pasteMetricsService: PasteMetricsService,
) {
    private val indexCoordinates: IndexCoordinates
        get() = IndexCoordinates.of(properties.elasticsearch.indexName)

    fun find(query: LegacyPasteQuery): List<PasteResponse> {
        val nativeQuery = NativeQuery.builder()
            .withQuery(elasticQuery(query))
            .withPageable(PageRequest.of(query.page - 1, query.pageLimit))
            .withSort(toSort(query))
            .build()

        val documents = operations.search(nativeQuery, ElasticPasteDocument::class.java, indexCoordinates)
            .searchHits
            .map { it.content }
        val metrics = pasteMetricsService.getMetrics(documents.map { it.key })
        return documents.map { map(it, query, metrics[it.key]) }
    }

    private fun elasticQuery(query: LegacyPasteQuery): ElasticQuery = ElasticQuery.of { root ->
        root.bool { bool ->
            query.search?.takeIf(String::isNotBlank)?.let { search ->
                bool.must { must ->
                    must.multiMatch { multi ->
                        multi.query(search)
                            .fields("title^3", "content", "user.name", "user.uniqueName")
                            .fuzziness("2")
                            .prefixLength(0)
                            .maxExpansions(30)
                            .minimumShouldMatch("1")
                    }
                }
            }
            if (query.filterTags.isNotEmpty()) {
                bool.must(termsQuery("tags", query.filterTags))
            }
            query.filter?.let { filter ->
                bool.filter(filterQuery(filter))
            }
            bool
        }
    }

    private fun filterQuery(filter: LegacyFilter): ElasticQuery = when (filter) {
        is LegacyFilterGroup -> ElasticQuery.of { query ->
            query.bool { bool ->
                when (filter.operator) {
                    LegacyGroupOperator.AND -> filter.children.forEach { child ->
                        bool.must(filterQuery(child))
                    }
                    LegacyGroupOperator.OR -> {
                        filter.children.forEach { child -> bool.should(filterQuery(child)) }
                        if (filter.children.isNotEmpty()) bool.minimumShouldMatch("1")
                    }
                }
                bool
            }
        }
        is LegacyFieldFilter -> fieldQuery(filter)
    }

    private fun fieldQuery(filter: LegacyFieldFilter): ElasticQuery {
        val field = normalizeField(filter.field)
        return when (filter.operator) {
            LegacyFilterOperator.EQ -> filter.value?.let { termQuery(field, it) } ?: existsQuery(field, mustExist = false)
            LegacyFilterOperator.NE -> ElasticQuery.of { query ->
                query.bool { bool -> bool.mustNot(termQuery(field, filter.value.orEmpty())) }
            }
            LegacyFilterOperator.NULL -> existsQuery(field, mustExist = false)
            LegacyFilterOperator.NOT_NULL -> existsQuery(field, mustExist = true)
            LegacyFilterOperator.GT -> rangeQuery(field, filter.value, "gt")
            LegacyFilterOperator.GTE -> rangeQuery(field, filter.value, "gte")
            LegacyFilterOperator.LT -> rangeQuery(field, filter.value, "lt")
            LegacyFilterOperator.LTE -> rangeQuery(field, filter.value, "lte")
        }
    }

    private fun termQuery(field: String, value: String): ElasticQuery =
        if (field == "tags") {
            termsQuery(field, listOf(value))
        } else {
            ElasticQuery.of { query -> query.term { term -> term.field(field).value(value) } }
        }

    private fun termsQuery(field: String, values: List<String>): ElasticQuery =
        ElasticQuery.of { query -> query.terms { terms ->
            terms.field(field)
                .terms(TermsQueryField.of { termsField ->
                    termsField.value(values.map(FieldValue::of))
                })
        } }

    private fun existsQuery(field: String, mustExist: Boolean): ElasticQuery =
        if (mustExist) {
            ElasticQuery.of { query -> query.exists { exists -> exists.field(field) } }
        } else {
            ElasticQuery.of { query ->
                query.bool { bool -> bool.mustNot { mustNot -> mustNot.exists { exists -> exists.field(field) } } }
            }
        }

    private fun rangeQuery(field: String, value: String?, operator: String): ElasticQuery = ElasticQuery.of { query ->
        query.range { range ->
            range.term { term ->
                term.field(field)
                when (operator) {
                    "gt" -> term.gt(value)
                    "gte" -> term.gte(value)
                    "lt" -> term.lt(value)
                    else -> term.lte(value)
                }
            }
        }
    }

    private fun toSort(query: LegacyPasteQuery): Sort {
        val orders = query.sorts.map {
            Sort.Order(
                if (it.ascending) Sort.Direction.ASC else Sort.Direction.DESC,
                normalizeField(it.field),
            )
        }
        return if (orders.isEmpty()) Sort.unsorted() else Sort.by(orders)
    }

    private fun map(document: ElasticPasteDocument, query: LegacyPasteQuery, metrics: PasteMetrics?): PasteResponse =
        PasteResponse(
            exists = true,
            id = document.key,
            rawUrl = "${properties.serverName.trimEnd('/')}/${document.key}/raw",
            title = document.title,
            content = document.content.let { if (query.shortenContent) it.shorten() else it },
            createdAt = document.createdAt?.toString() ?: "0000-00-00 00:00:00",
            expireAt = document.expireAt?.toString(),
            encrypted = document.encrypted,
            userId = document.userId,
            forkedFrom = document.forkedFrom,
            visibility = document.visibility,
            folder = document.folder,
            tags = document.tags,
            type = document.type,
            user = document.user?.let {
                PublicUserDto(id = it.id, name = it.uniqueName, displayName = it.name, avatar = it.avatar)
            },
            starCount = metrics?.starCount ?: 0,
            commentCount = metrics?.commentCount ?: 0,
            viewCount = metrics?.viewCount ?: 0,
        )

    private fun normalizeField(field: String): String = FIELD_ALIASES[field] ?: field

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
            "user.email" to "user.eMail",
            "user_email" to "user.eMail",
            "user.unique_name" to "user.uniqueName",
            "user.auth_id" to "user.authId",
            "user.auth_provider" to "user.authProvider",
        )
    }
}
