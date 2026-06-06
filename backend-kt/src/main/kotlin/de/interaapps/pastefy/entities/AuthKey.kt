package de.interaapps.pastefy.entities

import de.interaapps.pastefy.exceptions.PermissionsDeniedException
import de.interaapps.pastefy.util.RandomStrings
import jakarta.persistence.*
import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type as HibernateType
import java.time.Instant

@Entity
@Table(
    name = "pastefy_auth_keys",
    indexes = [
        Index(name = "pastefy_auth_keys_key_index", columnList = "`key`"),
        Index(name = "pastefy_auth_keys_user_id_index", columnList = "user_id"),
    ],
)
class AuthKey(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "`key`", length = 60, nullable = true, unique = true)
    var key: String = RandomStrings.alphanumeric(60),

    @Column(length = 255)
    var accessToken: String? = null,

    @Column(length = 255)
    var refreshToken: String? = null,

    @Column(length = 8, nullable = true)
    var userId: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var type: Type = Type.USER,

    @HibernateType(JsonType::class)
    @Column(columnDefinition = "json")
    var scopes: MutableList<String>? = null,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(nullable = false)
    var updatedAt: Instant? = null,
) {
    fun hasPermission(permission: String): Boolean {
        if (permission.isBlank()) return true
        return hasScope(permission) || hasScope(permission.substringBefore(':'))
    }

    fun checkPermissions(vararg permissions: String) {
        if (permissions.none(::hasPermission)) throw PermissionsDeniedException()
    }

    fun addScope(scope: String): AuthKey = apply {
        val currentScopes = scopes ?: mutableListOf<String>().also { scopes = it }
        currentScopes += scope
    }

    private fun hasScope(scope: String): Boolean = type != Type.ACCESS_TOKEN || scopes?.contains(scope) == true

    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        if (createdAt == null) createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    enum class Type { API, USER, ACCESS_TOKEN }
}
