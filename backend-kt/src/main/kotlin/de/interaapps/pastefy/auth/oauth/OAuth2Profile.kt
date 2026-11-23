package de.interaapps.pastefy.auth.oauth

data class OAuth2Profile(
    val id: String,
    val name: String,
    val email: String? = null,
    val avatar: String? = null,
)

data class OAuth2Tokens(
    val accessToken: String,
    val refreshToken: String? = null,
)
