package de.interaapps.pastefy.auth.oauth.providers

import de.interaapps.pastefy.auth.oauth.*

class GoogleOAuth2Provider(
    private val clientId: String = "",
    private val clientSecret: String = "",
    private val scopes: List<String> = listOf("https://www.googleapis.com/auth/userinfo.profile", "https://www.googleapis.com/auth/userinfo.email"),
    private val http: OAuthHttpClient? = null,
) : OAuth2Provider {
    override val name = "google"

    override fun authorizationUrl(callbackUrl: String, state: String): String = authorizationUrl(
        "https://accounts.google.com/o/oauth2/v2/auth",
        mapOf("response_type" to "code", "client_id" to clientId, "redirect_uri" to callbackUrl, "scope" to scopes.joinToString(" "), "access_type" to "offline", "prompt" to "consent", "state" to state),
    )

    override fun exchangeCode(code: String, callbackUrl: String): OAuth2Tokens = requireHttp().postForm(
        "https://oauth2.googleapis.com/token",
        mapOf("client_id" to clientId, "client_secret" to clientSecret, "code" to code, "grant_type" to "authorization_code", "redirect_uri" to callbackUrl),
    ).tokens()

    override fun loadProfile(tokens: OAuth2Tokens): OAuth2Profile {
        val data = requireHttp().get("https://www.googleapis.com/oauth2/v2/userinfo", mapOf("Authorization" to "Bearer ${tokens.accessToken}"))
        return OAuth2Profile(data.requiredText("id"), data.requiredText("name"), data.optionalText("email"), data.optionalText("picture"))
    }

    private fun requireHttp() = requireNotNull(http) { "OAuth HTTP client is required" }
}
