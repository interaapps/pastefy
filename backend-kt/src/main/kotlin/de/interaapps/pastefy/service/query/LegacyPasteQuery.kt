package de.interaapps.pastefy.service.query

import de.interaapps.pastefy.entities.User

data class LegacyPasteQuery(
    val page: Int,
    val pageLimit: Int,
    val search: String?,
    val sorts: List<LegacySort>,
    val filter: LegacyFilter?,
    val filterTags: List<String>,
    val shortenContent: Boolean,
    val withAiInfo: Boolean,
    val currentUser: User?,
) {
    val offset: Int get() = pageLimit * (page - 1)
}

data class LegacySort(
    val field: String,
    val ascending: Boolean,
)

sealed interface LegacyFilter

data class LegacyFilterGroup(
    val operator: LegacyGroupOperator,
    val children: List<LegacyFilter>,
) : LegacyFilter

data class LegacyFieldFilter(
    val field: String,
    val operator: LegacyFilterOperator,
    val value: String? = null,
) : LegacyFilter

enum class LegacyGroupOperator { AND, OR }

enum class LegacyFilterOperator(val legacyName: String) {
    EQ("\$eq"),
    NE("\$ne"),
    NULL("\$null"),
    NOT_NULL("\$notNull"),
    GT("\$gt"),
    GTE("\$gte"),
    LT("\$lt"),
    LTE("\$lte");

    companion object {
        fun fromLegacyName(value: String): LegacyFilterOperator? = entries.firstOrNull { it.legacyName == value }
    }
}
