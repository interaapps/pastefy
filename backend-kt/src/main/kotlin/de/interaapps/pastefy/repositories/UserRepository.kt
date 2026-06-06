package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface UserRepository : JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    fun findByUniqueName(uniqueName: String): User?

    fun existsByUniqueName(uniqueName: String): Boolean

    fun findByAuthIdAndAuthProvider(
        authId: String,
        authProvider: User.AuthenticationProvider
    ): User?

}
