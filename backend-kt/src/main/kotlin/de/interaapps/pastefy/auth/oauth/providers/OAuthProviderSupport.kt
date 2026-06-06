package de.interaapps.pastefy.auth.oauth.providers

import com.fasterxml.jackson.databind.JsonNode
import de.interaapps.pastefy.auth.oauth.OAuth2Tokens
import de.interaapps.pastefy.exceptions.OAuth2Exception
import okhttp3.HttpUrl.Companion.toHttpUrl

internal fun authorizationUrl(endpoint: String, fields: Map<String, String>): String =
    endpoint.toHttpUrl().newBuilder().apply { fields.forEach { (name, value) -> addQueryParameter(name, value) } }
        .build().toString()

internal fun JsonNode.requiredText(name: String): String =
    path(name).takeUnless { it.isMissingNode || it.isNull }?.asText()?.takeIf { it.isNotBlank() }
        ?: throw OAuth2Exception("OAuth2 provider response is missing $name")

internal fun JsonNode.optionalText(name: String): String? =
    path(name).takeUnless { it.isMissingNode || it.isNull }?.asText()?.takeIf { it.isNotBlank() }

internal fun JsonNode.tokens(): OAuth2Tokens = OAuth2Tokens(requiredText("access_token"), optionalText("refresh_token"))
