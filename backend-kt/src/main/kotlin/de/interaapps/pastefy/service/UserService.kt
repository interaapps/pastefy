package de.interaapps.pastefy.service

import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.model.database.*
import de.interaapps.pastefy.repositories.PasteRepository
import de.interaapps.pastefy.repositories.PasteStarRepository
import de.interaapps.pastefy.repositories.UserRepository
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
    // private val elasticStarsService: ElasticStarsService,
    // private val publicPasteEngagementService: PublicPasteEngagementService,
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
                    folder,
                    fetchChildren,
                    fetchSubChildren,
                    fetchPastes,
                    true
                )
            }
    }

    fun getFolderWithChildren(user: User): List<FolderResponse> {
        return folderRepository.findAllByUserIdAndParentIsNull(user.id)
            .map { folder ->
                FolderResponse(
                    folder,
                    true,
                    true,
                    false,
                    true
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

        // async {
        //     elasticStarsService.addStarCount(paste, user)
        //     publicPasteEngagementService.addInterestFromPaste(paste, 20)
        // }
    }

    @Transactional
    fun unstar(user: User, paste: Paste) {
        pasteStarRepository.deleteByPasteAndUserId(paste.key, user.id)

        // async {
        //     elasticStarsService.removeStarCount(paste, user)
        //     publicPasteEngagementService.addInterestFromPaste(paste, -20)
        // }
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