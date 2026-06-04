package de.interaapps.pastefy.service

import de.interaapps.pastefy.dto.pastes.CreatePasteRequest
import de.interaapps.pastefy.dto.pastes.EditPasteRequest
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.PasteTag
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.exceptions.NotFoundException
import de.interaapps.pastefy.exceptions.PermissionsDeniedException
import de.interaapps.pastefy.infrastructure.ai.PasteAIInfoService
import de.interaapps.pastefy.infrastructure.elastic.ElasticPasteService
import de.interaapps.pastefy.repositories.FolderRepository
import de.interaapps.pastefy.repositories.PasteRepository
import de.interaapps.pastefy.repositories.PasteTagRepository
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant

@Service
class PasteCommandService(
    private val pasteRepository: PasteRepository,
    private val folderRepository: FolderRepository,
    private val pasteTagRepository: PasteTagRepository,
    private val pasteService: PasteService,
    private val tagListings: TagListingService,
    private val engagement: PublicPasteEngagementService,
    private val elasticProvider: ObjectProvider<ElasticPasteService>,
    private val aiProvider: ObjectProvider<PasteAIInfoService>,
) {
    @Transactional
    fun create(request: CreatePasteRequest, user: User?): Paste {
        val paste = Paste(
            title = request.title,
            userId = user?.id,
            encrypted = request.encrypted,
            type = request.type,
            visibility = request.visibility,
            expireAt = parseExpireAt(request.expireAt),
            forkedFrom = request.forkedFrom?.takeIf { pasteRepository.existsByKey(it) },
            folder = request.folder?.let(folderRepository::findByKey)?.takeIf { it.userId == user?.id }?.key,
        ).apply { setDatabaseContent(request.content) }
        val saved = pasteService.save(paste)
        syncTags(saved, request.tags.orEmpty())
        request.forkedFrom?.let(pasteRepository::findByKey)?.takeIf(Paste::isPublic)?.let { engagement.addInterest(it, 10) }
        if (request.ai && !request.encrypted) aiProvider.ifAvailable?.enqueueIfEligible(saved, force = true)
        return saved
    }

    @Transactional
    fun createUploaded(title: String?, content: String, type: PasteType, user: User?, tags: List<String> = emptyList()): Paste {
        val paste = Paste(title = title, userId = user?.id, type = type).apply { setDatabaseContent(content) }
        val saved = pasteService.save(paste)
        syncTags(saved, tags)
        return saved
    }

    @Transactional
    fun update(key: String, request: EditPasteRequest, user: User): Paste {
        val paste = requireOwned(key, user)
        request.title?.let { paste.title = it }
        request.content?.let(paste::setDatabaseContent)
        request.folder?.let { paste.folder = folderRepository.findByKey(it)?.takeIf { folder -> folder.userId == user.id }?.key }
        request.type?.let { paste.type = it }
        request.encrypted?.let { paste.encrypted = it }
        request.visibility?.let { paste.visibility = it }
        request.expireAt?.let { paste.expireAt = parseExpireAt(it) }
        val saved = pasteService.save(paste)
        request.tags?.let { syncTags(saved, it) }
        return saved
    }

    @Transactional
    fun delete(key: String, user: User) = pasteService.delete(requireOwned(key, user))

    fun requireOwned(key: String, user: User): Paste {
        val paste = pasteRepository.findByKey(key) ?: throw NotFoundException()
        if (paste.userId != user.id && !user.isAdmin) throw PermissionsDeniedException()
        return paste
    }

    @Transactional
    fun syncTags(paste: Paste, requestedTags: List<String>) {
        val tags = requestedTags.asSequence().map { it.trim().take(30) }.filter(String::isNotEmpty).distinct().toSet()
        val existing = pasteTagRepository.findAllByPaste(paste.key).map(PasteTag::tag).toSet()
        (tags - existing).forEach { tag ->
            pasteTagRepository.save(PasteTag(paste = paste.key, tag = tag))
            tagListings.updateCount(tag)
        }
        (existing - tags).forEach { tag ->
            pasteTagRepository.deleteByPasteAndTag(paste.key, tag)
            tagListings.updateCount(tag)
        }
        elasticProvider.ifAvailable?.updateTags(paste)
    }

    private fun parseExpireAt(value: String?): Instant? {
        if (value == null || value.length < 16) return null
        return runCatching { Instant.parse(value) }
            .recoverCatching { Timestamp.valueOf(value).toInstant() }
            .getOrNull()
    }
}
