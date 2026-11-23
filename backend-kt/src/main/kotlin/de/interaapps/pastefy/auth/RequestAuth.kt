package de.interaapps.pastefy.auth

import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User

data class RequestAuth(
    val authKey: AuthKey,
    val user: User,
)
