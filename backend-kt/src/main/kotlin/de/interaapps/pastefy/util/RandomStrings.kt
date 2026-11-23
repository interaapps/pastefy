package de.interaapps.pastefy.util

import java.security.SecureRandom

object RandomStrings {
    private const val CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private val random = SecureRandom()

    fun alphanumeric(length: Int): String = buildString(length) {
        repeat(length) {
            append(CHARACTERS[random.nextInt(CHARACTERS.length)])
        }
    }
}
