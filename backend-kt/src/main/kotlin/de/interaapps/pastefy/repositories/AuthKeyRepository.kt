package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.AuthKey
import org.springframework.data.jpa.repository.JpaRepository

interface AuthKeyRepository : JpaRepository<AuthKey, Int> {
    fun findByKey(key: String): AuthKey?
    fun findAllByUserIdAndType(userId: String, type: AuthKey.Type): List<AuthKey>
    fun deleteByKeyAndUserId(key: String, userId: String)
    fun deleteByUserId(userId: String)
}
