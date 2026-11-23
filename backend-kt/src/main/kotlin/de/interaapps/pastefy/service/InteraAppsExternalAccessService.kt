package de.interaapps.pastefy.service

import de.interaapps.pastefy.auth.oauth.OAuth2ProviderRegistry
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.dto.auth.InteraAppsExternalAccessRequest
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.exceptions.AuthenticationException
import de.interaapps.pastefy.exceptions.NotFoundException
import de.interaapps.pastefy.repositories.AuthKeyRepository
import de.interaapps.pastefy.repositories.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InteraAppsExternalAccessService(
    private val properties: PastefyProperties,
    private val providers: OAuth2ProviderRegistry,
    private val userRepository: UserRepository,
    private val authKeyRepository: AuthKeyRepository,
) {
    @Transactional
    fun issue(request: InteraAppsExternalAccessRequest): String {
        if (providers.get("interaapps") == null) throw NotFoundException()
        val config = properties.oauth.interaapps
        if (config.clientId != request.appId || config.clientSecret != request.appSecret) throw AuthenticationException()
        val user = userRepository.findByAuthIdAndAuthProvider(request.userId, User.AuthenticationProvider.INTERAAPPS)
            ?: throw NotFoundException()
        return authKeyRepository.save(
            AuthKey(userId = user.id, type = AuthKey.Type.ACCESS_TOKEN, scopes = request.appScopeList.toMutableList()),
        ).key
    }
}
