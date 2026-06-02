package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {

    fun findByUniqueName(uniqueName: String): User?

    fun existsByUniqueName(uniqueName: String): Boolean

    fun findByAuthIdAndAuthProvider(
        authId: String,
        authProvider: User.AuthenticationProvider
    ): User?

}
