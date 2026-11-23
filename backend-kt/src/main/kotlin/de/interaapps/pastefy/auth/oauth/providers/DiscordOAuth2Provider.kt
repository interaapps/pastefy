package de.interaapps.pastefy.auth.oauth.providers

import de.interaapps.pastefy.auth.oauth.*

class DiscordOAuth2Provider(
    private val clientId: String = "",
    private val clientSecret: String = "",
    private val scopes: List<String> = listOf("email", "identify"),
    private val http: OAuthHttpClient? = null,
) : OAuth2Provider {
    override val name = "discord"

    override fun authorizationUrl(callbackUrl: String, state: String): String = authorizationUrl(
        "https://discord.com/api/oauth2/authorize",
        mapOf(
            "response_type" to "code",
            "client_id" to clientId,
            "prompt" to "consent",
            "scope" to scopes.joinToString(" "),
            "redirect_uri" to callbackUrl,
            "state" to state
        ),
    )

    override fun exchangeCode(code: String, callbackUrl: String): OAuth2Tokens = requireHttp().postForm(
        "https://discord.com/api/oauth2/token",
        mapOf(
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "code" to code,
            "grant_type" to "authorization_code",
            "redirect_uri" to callbackUrl,
            "scope" to scopes.joinToString(" ")
        ),
    ).tokens()

    override fun loadProfile(tokens: OAuth2Tokens): OAuth2Profile {
        val data = requireHttp().get(
            "https://discord.com/api/users/@me",
            mapOf("Authorization" to "Bearer ${tokens.accessToken}")
        )
        val id = data.requiredText("id")
        val avatar = data.optionalText("avatar")?.let { "https://cdn.discordapp.com/avatars/$id/$it.png" }
        return OAuth2Profile(id, data.requiredText("username"), data.optionalText("email"), avatar)
    }

    private fun requireHttp() = requireNotNull(http) { "OAuth HTTP client is required" }
}
