package de.interaapps.pastefy.auth.oauth.providers

import de.interaapps.pastefy.auth.oauth.*
import de.interaapps.pastefy.exceptions.OAuth2Exception

class InteraAppsOAuth2Provider(
    private val clientId: String = "",
    private val clientSecret: String = "",
    private val scopes: List<String> = listOf("user:read"),
    private val http: OAuthHttpClient? = null,
) : OAuth2Provider {
    override val name = "interaapps"

    override fun authorizationUrl(callbackUrl: String, state: String): String = authorizationUrl(
        "https://accounts.interaapps.de/auth/oauth2",
        mapOf(
            "client_id" to clientId,
            "scope" to scopes.joinToString(" "),
            "redirect_uri" to callbackUrl,
            "state" to state
        ),
    )

    override fun exchangeCode(code: String, callbackUrl: String): OAuth2Tokens {
        val data = requireHttp().postJson(
            "https://accounts.interaapps.de/api/v2/authorization/oauth2/access_token",
            mapOf("client_id" to clientId, "client_secret" to clientSecret, "code" to code),
        )
        if (!data.path("success").asBoolean(false)) throw OAuth2Exception()
        val returnedScopes = data.path("scope_list").map { it.asText() }.toSet()
        if (!returnedScopes.containsAll(scopes)) throw OAuth2Exception("InteraApps OAuth2 response is missing requested scopes")
        return data.tokens()
    }

    override fun loadProfile(tokens: OAuth2Tokens): OAuth2Profile {
        val user = requireHttp().get(
            "https://accounts.interaapps.de/api/v2/user",
            mapOf("Authorization" to "Bearer ${tokens.accessToken}")
        )
        return OAuth2Profile(
            user.requiredText("id"),
            user.requiredText("name"),
            user.optionalText("mail"),
            user.optionalText("profile_picture")
        )
    }

    private fun requireHttp() = requireNotNull(http) { "OAuth HTTP client is required" }
}
