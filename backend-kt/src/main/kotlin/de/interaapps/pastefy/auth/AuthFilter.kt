package de.interaapps.pastefy.auth

import de.interaapps.pastefy.repositories.AuthKeyRepository
import de.interaapps.pastefy.repositories.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets
import java.util.Base64

@Component
class AuthFilter(
    private val authKeyRepository: AuthKeyRepository,
    private val userRepository: UserRepository,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        extractAuthKey(request)
            ?.let(authKeyRepository::findByKey)
            ?.let { authKey ->
                userRepository.findById(authKey.userId)
                    .orElse(null)
                    ?.let { user -> request.setAttribute(RequestAuthAttributes.AUTH, RequestAuth(authKey, user)) }
            }

        filterChain.doFilter(request, response)
    }

    private fun extractAuthKey(request: HttpServletRequest): String? {
        request.getHeader("x-auth-key")?.takeIf { it.isNotBlank() }?.let { return it }
        val authorization = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        if (authorization.startsWith("Bearer ", ignoreCase = true)) {
            return authorization.substring(7).trim().takeIf { it.isNotBlank() }
        }
        if (authorization.startsWith("Basic ", ignoreCase = true)) {
            return runCatching {
                String(Base64.getDecoder().decode(authorization.substring(6)), StandardCharsets.UTF_8)
                    .substringAfter(':', "")
                    .takeIf { it.isNotBlank() }
            }.getOrNull()
        }
        return null
    }
}
