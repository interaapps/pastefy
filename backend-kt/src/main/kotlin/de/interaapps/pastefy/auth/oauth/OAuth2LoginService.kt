package de.interaapps.pastefy.auth.oauth

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.exceptions.OAuth2Exception
import de.interaapps.pastefy.repositories.AuthKeyRepository
import de.interaapps.pastefy.repositories.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OAuth2LoginService(
    private val properties: PastefyProperties,
    private val userRepository: UserRepository,
    private val authKeyRepository: AuthKeyRepository,
) {
    @Transactional
    fun login(provider: OAuth2Provider, tokens: OAuth2Tokens): AuthKey {
        val profile = provider.loadProfile(tokens)
        val authenticationProvider = User.AuthenticationProvider.entries.firstOrNull { it.providerName == provider.name }
            ?: throw OAuth2Exception("Unsupported OAuth2 provider")
        val user = userRepository.findByAuthIdAndAuthProvider(profile.id, authenticationProvider)
            ?: User(
                authId = profile.id,
                authProvider = authenticationProvider,
                uniqueName = availableUniqueName(profile.name),
                type = if (properties.grantAccessRequired) User.Type.AWAITING_ACCESS else User.Type.USER,
            )
        user.name = profile.name
        user.avatar = profile.avatar
        user.email = profile.email
        userRepository.save(user)
        return authKeyRepository.save(
            AuthKey(
                userId = user.id,
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken,
                type = AuthKey.Type.USER,
            )
        )
    }

    private fun availableUniqueName(name: String): String {
        val base = name.replace(Regex("[^a-zA-Z0-9]"), "").take(33).ifBlank { "user" }
        if (!userRepository.existsByUniqueName(base)) return base
        var suffix = 1
        while (true) {
            val candidate = base.take(33 - suffix.toString().length) + suffix
            if (!userRepository.existsByUniqueName(candidate)) return candidate
            suffix++
        }
    }
}
