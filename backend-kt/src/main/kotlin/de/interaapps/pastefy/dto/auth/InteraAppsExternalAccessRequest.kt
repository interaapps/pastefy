package de.interaapps.pastefy.dto.auth

data class InteraAppsExternalAccessRequest(
    val provider: String? = null,
    val appId: String,
    val appSecret: String,
    val scope: String? = null,
    val scopeList: List<String> = emptyList(),
    val appScopeList: List<String> = emptyList(),
    val userId: String,
)
