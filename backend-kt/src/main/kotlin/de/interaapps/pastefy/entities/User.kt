package de.interaapps.pastefy.entities

import de.interaapps.pastefy.auth.oauth.OAuth2Provider
import de.interaapps.pastefy.auth.oauth.providers.*
import de.interaapps.pastefy.util.RandomStrings
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "pastefy_users")
class User(
    @Id
    @Column(length = 8)
    var id: String = RandomStrings.alphanumeric(8),

    @Column
    var name: String? = null,

    @Column(length = 33)
    var uniqueName: String? = null,

    @Column(name = "email")
    var email: String? = null,

    @Column
    var avatar: String? = null,

    @Column(length = 455)
    var authId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var authProvider: AuthenticationProvider? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: Type = Type.USER,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(nullable = false)
    var updatedAt: Instant? = null

) {

    val isAdmin: Boolean
        get() = type == Type.ADMIN

    fun roleCheck(): Boolean {
        return type != Type.AWAITING_ACCESS && type != Type.BLOCKED
    }

    @PrePersist
    fun prePersist() {
        val now = Instant.now()

        if (createdAt == null) {
            createdAt = now
        }

        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }

    enum class AuthenticationProvider(
        val oauth2ServiceClass: Class<out OAuth2Provider>,
        val providerName: String
    ) {
        INTERAAPPS(InteraAppsOAuth2Provider::class.java, "interaapps"),
        GOOGLE(GoogleOAuth2Provider::class.java, "google"),
        GITHUB(GitHubOAuth2Provider::class.java, "github"),
        TWITCH(TwitchOAuth2Provider::class.java, "twitch"),
        OIDC(CustomOAuth2Provider::class.java, "oidc"),
        DISCORD(DiscordOAuth2Provider::class.java, "discord");

        companion object {
            fun getProviderByClass(
                oauth2ServiceClass: Class<out OAuth2Provider>
            ): AuthenticationProvider? {
                return entries.firstOrNull {
                    it.oauth2ServiceClass == oauth2ServiceClass
                }
            }
        }
    }

    enum class Type {
        USER,
        ADMIN,
        BLOCKED,
        AWAITING_ACCESS
    }

}
