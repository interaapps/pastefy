package de.interaapps.pastefy.auth.oauth

import de.interaapps.pastefy.auth.oauth.providers.*
import de.interaapps.pastefy.config.PastefyProperties
import org.springframework.stereotype.Component

@Component
class OAuth2ProviderRegistry(
    properties: PastefyProperties,
    http: OAuthHttpClient,
) {
    private val providers: Map<String, OAuth2Provider> = buildList<OAuth2Provider> {
        properties.oauth.interaapps.takeIf { it.enabled }?.let { add(InteraAppsOAuth2Provider(it.clientId, it.clientSecret, it.scopes.orDefault("user:read"), http)) }
        properties.oauth.google.takeIf { it.enabled }?.let { add(GoogleOAuth2Provider(it.clientId, it.clientSecret, it.scopes.orDefault("https://www.googleapis.com/auth/userinfo.profile", "https://www.googleapis.com/auth/userinfo.email"), http)) }
        properties.oauth.github.takeIf { it.enabled }?.let { add(GitHubOAuth2Provider(it.clientId, it.clientSecret, it.scopes.orDefault("read:user", "user:email"), http)) }
        properties.oauth.twitch.takeIf { it.enabled }?.let { add(TwitchOAuth2Provider(it.clientId, it.clientSecret, it.scopes.orDefault("user:read:email"), http)) }
        properties.oauth.discord.takeIf { it.enabled }?.let { add(DiscordOAuth2Provider(it.clientId, it.clientSecret, it.scopes.orDefault("email", "identify"), http)) }
        properties.oauth.oidc.takeIf { it.fullyConfigured }?.let {
            add(CustomOAuth2Provider(it.clientId, it.clientSecret, it.authorizationEndpoint, it.tokenEndpoint, it.userInfoEndpoint, it.scopes.orDefault("openid", "email", "profile"), http))
        }
    }.associateBy(OAuth2Provider::name)

    init {
        require(providers.isEmpty() || properties.oauthStateSecret.length >= 32) {
            "pastefy.oauth-state-secret must contain at least 32 characters when OAuth2 is enabled"
        }
    }

    fun get(name: String): OAuth2Provider? = providers[name]

    fun names(): Set<String> = providers.keys

    private fun List<String>.orDefault(vararg defaults: String): List<String> = ifEmpty { defaults.toList() }
}
