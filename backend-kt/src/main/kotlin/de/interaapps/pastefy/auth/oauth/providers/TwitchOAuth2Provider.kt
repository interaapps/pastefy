package de.interaapps.pastefy.auth.oauth.providers

import de.interaapps.pastefy.auth.oauth.*

class TwitchOAuth2Provider(
    private val clientId: String = "",
    private val clientSecret: String = "",
    private val scopes: List<String> = listOf("user:read:email"),
    private val http: OAuthHttpClient? = null,
) : OAuth2Provider {
    override val name = "twitch"

    override fun authorizationUrl(callbackUrl: String, state: String): String = authorizationUrl(
        "https://id.twitch.tv/oauth2/authorize",
        mapOf("client_id" to clientId, "response_type" to "code", "scope" to scopes.joinToString(" "), "redirect_uri" to callbackUrl, "state" to state),
    )

    override fun exchangeCode(code: String, callbackUrl: String): OAuth2Tokens = requireHttp().postForm(
        "https://id.twitch.tv/oauth2/token",
        mapOf("client_id" to clientId, "client_secret" to clientSecret, "code" to code, "grant_type" to "authorization_code", "redirect_uri" to callbackUrl),
    ).tokens()

    override fun loadProfile(tokens: OAuth2Tokens): OAuth2Profile {
        val data = requireHttp().get("https://api.twitch.tv/helix/users", mapOf("Authorization" to "Bearer ${tokens.accessToken}", "Client-Id" to clientId))
            .path("data").firstOrNull() ?: throw de.interaapps.pastefy.exceptions.OAuth2Exception("Twitch returned no user")
        return OAuth2Profile(data.requiredText("id"), data.requiredText("login"), data.optionalText("email"), data.optionalText("profile_image_url"))
    }

    private fun requireHttp() = requireNotNull(http) { "OAuth HTTP client is required" }
}
