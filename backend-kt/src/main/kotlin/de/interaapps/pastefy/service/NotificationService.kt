package de.interaapps.pastefy.service

import de.interaapps.pastefy.entities.Notification
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.repositories.NotificationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val repository: NotificationRepository,
) {
    @Transactional
    fun list(user: User, onlyNotReceived: Boolean, onlyNotRead: Boolean): List<Notification> {
        val notifications = when {
            onlyNotReceived && onlyNotRead -> repository.findAllByUserIdAndReceivedAndAlreadyRead(user.id, false, false)
            onlyNotReceived -> repository.findAllByUserIdAndReceived(user.id, false)
            onlyNotRead -> repository.findAllByUserIdAndAlreadyRead(user.id, false)
            else -> repository.findAllByUserId(user.id)
        }
        notifications.forEach { it.received = true }
        return repository.saveAll(notifications)
    }

    @Transactional
    fun readAll(user: User) {
        val unread = repository.findAllByUserIdAndAlreadyRead(user.id, false)
        unread.forEach {
            it.received = true
            it.alreadyRead = true
        }
        repository.saveAll(unread)
    }
}
