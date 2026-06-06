package de.interaapps.pastefy.auth.oauth.providers

import de.interaapps.pastefy.auth.oauth.*

class GitHubOAuth2Provider(
    private val clientId: String = "",
    private val clientSecret: String = "",
    private val scopes: List<String> = listOf("read:user", "user:email"),
    private val http: OAuthHttpClient? = null,
) : OAuth2Provider {
    override val name = "github"

    override fun authorizationUrl(callbackUrl: String, state: String): String = authorizationUrl(
        "https://github.com/login/oauth/authorize",
        mapOf(
            "client_id" to clientId,
            "scope" to scopes.joinToString(" "),
            "redirect_uri" to callbackUrl,
            "state" to state
        ),
    )

    override fun exchangeCode(code: String, callbackUrl: String): OAuth2Tokens = requireHttp().postForm(
        "https://github.com/login/oauth/access_token",
        mapOf("client_id" to clientId, "client_secret" to clientSecret, "code" to code, "redirect_uri" to callbackUrl),
        mapOf("Accept" to "application/json"),
    ).tokens()

    override fun loadProfile(tokens: OAuth2Tokens): OAuth2Profile {
        val headers =
            mapOf("Authorization" to "Bearer ${tokens.accessToken}", "Accept" to "application/vnd.github+json")
        val user = requireHttp().get("https://api.github.com/user", headers)
        val emails = if (user.optionalText("email") == null) requireHttp().get(
            "https://api.github.com/user/emails",
            headers
        ) else null
        val email = user.optionalText("email")
            ?: emails?.firstOrNull { it.path("primary").asBoolean(false) }?.optionalText("email")
            ?: emails?.firstOrNull()?.optionalText("email")
        return OAuth2Profile(
            user.requiredText("id"),
            user.optionalText("name") ?: user.requiredText("login"),
            email,
            user.optionalText("avatar_url")
        )
    }

    private fun requireHttp() = requireNotNull(http) { "OAuth HTTP client is required" }
}
