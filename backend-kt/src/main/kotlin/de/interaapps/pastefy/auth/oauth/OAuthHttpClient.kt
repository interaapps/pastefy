package de.interaapps.pastefy.auth.oauth

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.interaapps.pastefy.exceptions.OAuth2Exception
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Component

@Component
class OAuthHttpClient(
    private val client: OkHttpClient,
    private val objectMapper: ObjectMapper,
) {
    fun get(url: String, headers: Map<String, String> = emptyMap()): JsonNode =
        execute(Request.Builder().url(url).applyHeaders(headers).get().build())

    fun postForm(url: String, fields: Map<String, String>, headers: Map<String, String> = emptyMap()): JsonNode {
        val body = FormBody.Builder().apply { fields.forEach { (name, value) -> add(name, value) } }.build()
        return execute(Request.Builder().url(url).applyHeaders(headers).post(body).build())
    }

    fun postJson(url: String, fields: Map<String, String>, headers: Map<String, String> = emptyMap()): JsonNode {
        val body = objectMapper.writeValueAsBytes(fields).toRequestBody(JSON)
        return execute(Request.Builder().url(url).applyHeaders(headers).post(body).build())
    }

    private fun execute(request: Request): JsonNode = client.newCall(request).execute().use { response ->
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw OAuth2Exception("OAuth2 provider request failed with status ${response.code}")
        runCatching { objectMapper.readTree(body) }
            .getOrElse { throw OAuth2Exception("OAuth2 provider returned invalid JSON") }
    }

    private fun Request.Builder.applyHeaders(headers: Map<String, String>) = apply {
        headers.forEach { (name, value) -> addHeader(name, value) }
        header("User-Agent", "PastefyOAuth2Client/1")
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
