package de.interaapps.pastefy.service

import de.interaapps.pastefy.entities.BackgroundJob
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.StorageType
import de.interaapps.pastefy.exceptions.NotFoundException
import de.interaapps.pastefy.exceptions.PastePrivateException
import de.interaapps.pastefy.infrastructure.elastic.ElasticPasteService
import de.interaapps.pastefy.infrastructure.redis.PasteRedisCacheService
import de.interaapps.pastefy.infrastructure.s3.S3PasteService
import de.interaapps.pastefy.repositories.BackgroundJobRepository
import de.interaapps.pastefy.repositories.PasteAIInfoRepository
import de.interaapps.pastefy.repositories.PasteCommentRepository
import de.interaapps.pastefy.repositories.PasteRepository
import de.interaapps.pastefy.repositories.PasteStarRepository
import de.interaapps.pastefy.repositories.PasteTagRepository
import de.interaapps.pastefy.repositories.PublicPasteEngagementRepository
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PasteService(
    private val pasteRepository: PasteRepository,
    private val pasteTagRepository: PasteTagRepository,
    private val pasteStarRepository: PasteStarRepository,
    private val pasteCommentRepository: PasteCommentRepository,
    private val pasteAIInfoRepository: PasteAIInfoRepository,
    private val backgroundJobRepository: BackgroundJobRepository,
    private val engagementRepository: PublicPasteEngagementRepository,
    private val redisCacheProvider: ObjectProvider<PasteRedisCacheService>,
    private val s3Provider: ObjectProvider<S3PasteService>,
    private val elasticProvider: ObjectProvider<ElasticPasteService>,
) {
    fun get(pasteKey: String): Paste? = pasteRepository.findByKey(pasteKey)

    fun getAccessiblePasteOrFail(pasteKey: String, user: User?): Paste {
        val paste = pasteRepository.findByKey(pasteKey) ?: throw NotFoundException()
        if (paste.isPrivate && (user == null || user.id != paste.userId)) throw PastePrivateException()
        return paste
    }

    fun getContent(paste: Paste, withCache: Boolean = true): String? {
        val redisCache = redisCacheProvider.ifAvailable
        var accessCount = 0L
        if (withCache) {
            accessCount = redisCache?.incrementAccessCount(paste) ?: 0
            redisCache?.getContent(paste)?.let { return it }
        }

        val content = if (paste.storageType == StorageType.S3) {
            if (paste.cachedContents == null) paste.cachedContents = requireS3().getContent(paste)
            paste.cachedContents
        } else {
            paste.content
        }

        if (withCache && content != null && redisCache?.shouldCache(accessCount) == true) {
            redisCache.putContent(paste, content)
        }
        return content
    }

    @Transactional
    fun save(paste: Paste): Paste {
        var saved = pasteRepository.saveAndFlush(paste)
        redisCacheProvider.ifAvailable?.evictContent(saved)

        val s3 = s3Provider.ifAvailable
        if (s3 != null && s3.shouldStore(saved)) {
            val content = getContent(saved, withCache = false).orEmpty()
            val reference = s3.store(saved, content)
            saved.cachedContents = content
            saved.setStorageReference(s3.encode(reference), StorageType.S3)
            saved = pasteRepository.saveAndFlush(saved)
        }

        if (elasticProvider.ifAvailable?.store(saved, getContent(saved, withCache = false)) == true) {
            pasteRepository.updateIndexedInElastic(requireNotNull(saved.id), true)
            saved.indexedInElastic = true
        }
        return saved
    }

    @Transactional
    fun delete(paste: Paste) {
        if (paste.storageType == StorageType.S3) requireS3().delete(paste)
        if (paste.indexedInElastic) elasticProvider.ifAvailable?.delete(paste)

        val pasteId = requireNotNull(paste.id)
        pasteCommentRepository.deleteByPaste(paste.key)
        pasteTagRepository.deleteByPaste(paste.key)
        pasteStarRepository.deleteByPaste(paste.key)
        engagementRepository.deleteByPasteId(pasteId)
        pasteAIInfoRepository.deleteById(pasteId)
        backgroundJobRepository.deleteByTypeAndEntityId(BackgroundJob.Type.PASTE_AI_INFO, pasteId)
        pasteRepository.delete(paste)
        redisCacheProvider.ifAvailable?.evictContent(paste)
    }

    private fun requireS3(): S3PasteService =
        s3Provider.ifAvailable ?: error("S3 storage is required to access paste content but pastefy.s3.enabled is false")
}
