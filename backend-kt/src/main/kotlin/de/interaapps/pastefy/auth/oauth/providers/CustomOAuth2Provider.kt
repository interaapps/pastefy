package de.interaapps.pastefy.auth.oauth.providers

import de.interaapps.pastefy.auth.oauth.*

class CustomOAuth2Provider(
    private val clientId: String = "",
    private val clientSecret: String = "",
    private val authorizationEndpoint: String = "",
    private val tokenEndpoint: String = "",
    private val userInfoEndpoint: String = "",
    private val scopes: List<String> = listOf("openid", "email", "profile"),
    private val http: OAuthHttpClient? = null,
) : OAuth2Provider {
    override val name = "oidc"

    override fun authorizationUrl(callbackUrl: String, state: String): String = authorizationUrl(
        authorizationEndpoint,
        mapOf(
            "response_type" to "code",
            "client_id" to clientId,
            "redirect_uri" to callbackUrl,
            "scope" to scopes.joinToString(" "),
            "state" to state
        ),
    )

    override fun exchangeCode(code: String, callbackUrl: String): OAuth2Tokens = requireHttp().postForm(
        tokenEndpoint,
        mapOf(
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "code" to code,
            "grant_type" to "authorization_code",
            "redirect_uri" to callbackUrl
        ),
    ).tokens()

    override fun loadProfile(tokens: OAuth2Tokens): OAuth2Profile {
        val profile = requireHttp().get(userInfoEndpoint, mapOf("Authorization" to "Bearer ${tokens.accessToken}"))
        return OAuth2Profile(
            profile.requiredText("sub"),
            profile.requiredText("name"),
            profile.optionalText("email"),
            profile.optionalText("picture")
        )
    }

    private fun requireHttp() = requireNotNull(http) { "OAuth HTTP client is required" }
}
