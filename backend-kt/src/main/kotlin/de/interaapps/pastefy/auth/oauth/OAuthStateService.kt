package de.interaapps.pastefy.auth.oauth

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.exceptions.OAuth2Exception
import de.interaapps.pastefy.util.RandomStrings
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class OAuthStateService(
    private val properties: PastefyProperties,
) {
    fun create(): String {
        val payload = "${Instant.now().epochSecond}.${RandomStrings.alphanumeric(48)}"
        return "$payload.${sign(payload)}"
    }

    fun validate(state: String?, cookieState: String?) {
        if (state.isNullOrBlank() || cookieState.isNullOrBlank()) throw OAuth2Exception("Missing OAuth2 state")
        if (!MessageDigest.isEqual(state.toByteArray(), cookieState.toByteArray())) throw OAuth2Exception("Invalid OAuth2 state")
        val payload = state.substringBeforeLast('.', "")
        val signature = state.substringAfterLast('.', "")
        if (payload.isBlank() || !MessageDigest.isEqual(sign(payload).toByteArray(), signature.toByteArray())) throw OAuth2Exception("Invalid OAuth2 state")
        val issuedAt = payload.substringBefore('.').toLongOrNull() ?: throw OAuth2Exception("Invalid OAuth2 state")
        if (Instant.now().epochSecond - issuedAt !in 0..600) throw OAuth2Exception("Expired OAuth2 state")
    }

    private fun sign(payload: String): String {
        val secret = properties.oauthStateSecret.takeIf { it.isNotBlank() }
            ?: throw OAuth2Exception("OAuth2 state secret is not configured")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8)))
    }
}
