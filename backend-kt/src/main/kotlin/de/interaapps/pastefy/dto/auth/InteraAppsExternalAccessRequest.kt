package de.interaapps.pastefy.dto.auth

import jakarta.validation.constraints.NotBlank

data class InteraAppsExternalAccessRequest(
    val provider: String? = null,
    @field:NotBlank val appId: String,
    @field:NotBlank val appSecret: String,
    val scope: String? = null,
    val scopeList: List<String> = emptyList(),
    val appScopeList: List<String> = emptyList(),
    @field:NotBlank val userId: String,
)
