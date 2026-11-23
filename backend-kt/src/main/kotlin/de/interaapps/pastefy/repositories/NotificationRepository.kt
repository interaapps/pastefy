package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Int> {
    fun findAllByUserId(userId: String): List<Notification>
    fun findAllByUserIdAndReceived(userId: String, received: Boolean): List<Notification>
    fun findAllByUserIdAndAlreadyRead(userId: String, alreadyRead: Boolean): List<Notification>
    fun findAllByUserIdAndReceivedAndAlreadyRead(
        userId: String,
        received: Boolean,
        alreadyRead: Boolean
    ): List<Notification>

    fun deleteByUserId(userId: String)
}
