package de.interaapps.pastefy.dto.app

data class AppInfoResponse(
    val customLogo: String? = null,
    val customName: String? = null,
    val aiEnabled: Boolean = false,
    val analyticsEnabled: Boolean = false,
    val customFooter: Map<String, String> = emptyMap(),
    val loginRequiredForRead: Boolean = false,
    val loginRequiredForCreate: Boolean = false,
    val encryptionIsDefault: Boolean = false,
    val publicPastesEnabled: Boolean = false,
)

data class StatsResponse(
    val createdPastes: Int = 0,
    val loggedInPastes: Int = 0,
    val userCount: Int = 0,
    val tagCount: Int = 0,
    val folderCount: Int = 0,
    val indexedPastes: Long? = null,
    val s3Pastes: Int? = null,
    val databasePastes: Int? = null,
)
