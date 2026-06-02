package de.interaapps.pastefy.service

import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.dto.folder.FolderResponse
import de.interaapps.pastefy.entities.Folder
import de.interaapps.pastefy.entities.Notification
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.PasteStar
import de.interaapps.pastefy.infrastructure.elastic.ElasticPasteService
import de.interaapps.pastefy.repositories.PasteRepository
import de.interaapps.pastefy.repositories.PasteStarRepository
import de.interaapps.pastefy.repositories.UserRepository
import de.interaapps.pastefy.repositories.AuthKeyRepository
import de.interaapps.pastefy.repositories.FolderRepository
import de.interaapps.pastefy.repositories.NotificationRepository
import de.interaapps.pastefy.repositories.SharedPasteRepository
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val pasteRepository: PasteRepository,
    private val folderRepository: FolderRepository,
    private val pasteStarRepository: PasteStarRepository,
    private val authKeyRepository: AuthKeyRepository,
    private val notificationRepository: NotificationRepository,
    private val sharedPasteRepository: SharedPasteRepository,
    private val elasticProvider: ObjectProvider<ElasticPasteService>,
) {

    fun get(id: String): User? {
        return userRepository.findById(id).orElse(null)
    }

    fun getByName(name: String): User? {
        return userRepository.findByUniqueName(name)
    }

    fun getPastes(user: User): List<Paste> {
        return pasteRepository.findAllByUserId(user.id)
    }

    fun getFolders(user: User): List<Folder> {
        return folderRepository.findAllByUserId(user.id)
    }

    fun getFolderTree(
        user: User,
        fetchChildren: Boolean,
        fetchSubChildren: Boolean,
        fetchPastes: Boolean
    ): List<FolderResponse> {
        return folderRepository.findAllByUserIdAndParentIsNull(user.id)
            .map { folder ->
                FolderResponse(
                    id = folder.key,
                    name = folder.name,
                    userId = folder.userId,
                    created = folder.createdAt?.toString() ?: "0000-00-00 00:00:00",
                    exists = true,
                )
            }
    }

    fun getFolderWithChildren(user: User): List<FolderResponse> {
        return folderRepository.findAllByUserIdAndParentIsNull(user.id)
            .map { folder ->
                FolderResponse(
                    id = folder.key,
                    name = folder.name,
                    userId = folder.userId,
                    created = folder.createdAt?.toString() ?: "0000-00-00 00:00:00",
                    exists = true,
                )
            }
    }

    @Transactional
    fun sendNotification(user: User, notification: Notification): Notification {
        notification.userId = user.id
        return notificationRepository.save(notification)
    }

    fun hasStarred(user: User, paste: Paste): Boolean {
        return pasteStarRepository.existsByPasteAndUserId(paste.key, user.id)
    }

    @Transactional
    fun star(user: User, paste: Paste) {
        if (hasStarred(user, paste)) {
            return
        }

        val pasteStar = PasteStar(
            paste = paste.key,
            userId = user.id
        )

        pasteStarRepository.save(pasteStar)
        elasticProvider.ifAvailable?.updateStars(paste)
    }

    @Transactional
    fun unstar(user: User, paste: Paste) {
        pasteStarRepository.deleteByPasteAndUserId(paste.key, user.id)
        elasticProvider.ifAvailable?.updateStars(paste)
    }

    @Transactional
    fun delete(user: User) {
        pasteRepository.findAllByUserId(user.id)
            .forEach { pasteRepository.delete(it) }

        folderRepository.deleteByUserId(user.id)
        authKeyRepository.deleteByUserId(user.id)
        notificationRepository.deleteByUserId(user.id)
        sharedPasteRepository.deleteByTargetIdOrUserId(user.id, user.id)

        userRepository.delete(user)
    }
}
