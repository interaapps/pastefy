package de.interaapps.pastefy.controller.auth

import de.interaapps.pastefy.auth.oauth.OAuth2LoginService
import de.interaapps.pastefy.auth.oauth.OAuth2ProviderRegistry
import de.interaapps.pastefy.auth.oauth.OAuthStateService
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.exceptions.NotFoundException
import de.interaapps.pastefy.exceptions.OAuth2Exception
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/auth/oauth2")
class OAuth2Controller(
    private val properties: PastefyProperties,
    private val registry: OAuth2ProviderRegistry,
    private val stateService: OAuthStateService,
    private val loginService: OAuth2LoginService,
) {
    @GetMapping("/{provider}")
    fun redirect(@PathVariable provider: String): ResponseEntity<Void> {
        val oauth = registry.get(provider) ?: throw NotFoundException("OAuth2 provider not found")
        val state = stateService.create()
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.SET_COOKIE, stateCookie(state).toString())
            .header(HttpHeaders.LOCATION, oauth.authorizationUrl(callbackUrl(provider), state))
            .build()
    }

    @GetMapping("/{provider}/callback")
    fun callback(
        @PathVariable provider: String,
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) error: String?,
        request: HttpServletRequest,
    ): ResponseEntity<Void> {
        if (!error.isNullOrBlank()) throw OAuth2Exception("OAuth2 provider rejected authentication")
        stateService.validate(state, request.cookies?.firstOrNull { it.name == STATE_COOKIE }?.value)
        val oauth = registry.get(provider) ?: throw NotFoundException("OAuth2 provider not found")
        val tokens = oauth.exchangeCode(code ?: throw OAuth2Exception("Missing OAuth2 authorization code"), callbackUrl(provider))
        val authKey = loginService.login(oauth, tokens)
        val encodedKey = URLEncoder.encode(authKey.key, StandardCharsets.UTF_8)
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.SET_COOKIE, stateCookie("", 0).toString())
            .header(HttpHeaders.LOCATION, "/auth?key=$encodedKey")
            .build()
    }

    private fun callbackUrl(provider: String) =
        "${properties.oauth.callbackBaseUrl.trimEnd('/')}/auth/oauth2/$provider/callback"

    private fun stateCookie(value: String, maxAgeSeconds: Long = 600): ResponseCookie =
        ResponseCookie.from(STATE_COOKIE, value)
            .httpOnly(true)
            .secure(properties.oauth.callbackBaseUrl.startsWith("https://"))
            .sameSite("Lax")
            .path("/auth/oauth2")
            .maxAge(maxAgeSeconds)
            .build()

    companion object {
        private const val STATE_COOKIE = "pastefy_oauth_state"
    }
}
