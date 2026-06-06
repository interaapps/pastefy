package de.interaapps.pastefy.service

import de.interaapps.pastefy.auth.oauth.OAuth2Provider
import de.interaapps.pastefy.auth.oauth.OAuth2ProviderRegistry
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.dto.auth.InteraAppsExternalAccessRequest
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.repositories.AuthKeyRepository
import de.interaapps.pastefy.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class InteraAppsExternalAccessServiceTest {
    @Test
    fun `creates access token with requested app scopes`() {
        val properties = PastefyProperties().apply {
            oauth.interaapps.clientId = "app-id"
            oauth.interaapps.clientSecret = "app-secret"
        }
        val providers = mock(OAuth2ProviderRegistry::class.java)
        val users = mock(UserRepository::class.java)
        val keys = mock(AuthKeyRepository::class.java)
        `when`(providers.get("interaapps")).thenReturn(mock(OAuth2Provider::class.java))
        `when`(users.findByAuthIdAndAuthProvider("external-user", User.AuthenticationProvider.INTERAAPPS))
            .thenReturn(User(id = "user-123", authId = "external-user", authProvider = User.AuthenticationProvider.INTERAAPPS))
        `when`(keys.save(any(AuthKey::class.java))).thenAnswer { invocation ->
            invocation.getArgument<AuthKey>(0).apply { key = "issued-key" }
        }

        val key = InteraAppsExternalAccessService(properties, providers, users, keys).issue(
            InteraAppsExternalAccessRequest(
                appId = "app-id",
                appSecret = "app-secret",
                appScopeList = listOf("pastes:read", "comments:create"),
                userId = "external-user",
            ),
        )

        assertEquals("issued-key", key)
    }
}
