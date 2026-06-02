package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Int> {
    fun findAllByUserId(userId: String): List<Notification>
    fun deleteByUserId(userId: String)
}
