package de.interaapps.pastefy.auth.oauth

interface OAuth2Provider {
    val name: String
    fun authorizationUrl(callbackUrl: String, state: String): String
    fun exchangeCode(code: String, callbackUrl: String): OAuth2Tokens
    fun loadProfile(tokens: OAuth2Tokens): OAuth2Profile
}
