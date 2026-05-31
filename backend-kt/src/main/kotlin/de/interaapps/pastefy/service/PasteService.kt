package de.interaapps.pastefy.service

import de.interaapps.pastefy.enums.StorageType
import de.interaapps.pastefy.model.database.Paste
import de.interaapps.pastefy.repositories.PasteRepository
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PasteService(
    private val pasteRepository: PasteRepository,
    // private val minioPasteService: MinioPasteService,
    // private val elasticPasteService: ElasticPasteService,
    // private val pasteContentCache: PasteContentCache,
    // private val pasteAccessCache: PasteAccessCache,
) {

    fun get(pasteKey: String): Paste? {
        return pasteRepository.findByKey(pasteKey)
    }

    fun getAccessiblePasteOrFail(pasteKey: String, user: User?): Paste {
        val paste = pasteRepository.findByKey(pasteKey)
            ?: throw NotFoundException()

        if (paste.isPrivate && (user == null || user.id != paste.userId)) {
            throw PastePrivateException()
        }

        return paste
    }

    fun getContent(paste: Paste, withCache: Boolean = true): String? {
        if (withCache) {
            // pasteAccessCache.increaseAccessCount(paste)

            // if (pasteAccessCache.getAccessCount(paste) > 10) {
            //     pasteContentCache.setCachedContent(paste)
            // }

            // pasteContentCache.getCachedContent(paste)?.let {
            //     return it
            // }
        }

        if (paste.storageType == StorageType.S3) {
            if (paste.cachedContents == null) {
                // paste.cachedContents = minioPasteService.getText(paste)
            }

            return paste.cachedContents
        }

        return paste.content
    }

    @Transactional
    fun save(paste: Paste): Paste {
        val saved = pasteRepository.save(paste)

        // pasteContentCache.deleteCachedContent(saved)

        // val threshold = config.getInt("minio.pastesize.threshold", -1)
        // val bigEnoughForS3 = saved.content != null && saved.content!!.length > threshold

        // if (minioEnabled && (saved.storageType == Paste.StorageType.S3 || bigEnoughForS3)) {
        //     minioPasteService.store(saved)
        // }

        // if (elasticsearchEnabled) {
        //     elasticPasteService.store(saved)
        // }

        return saved
    }

    @Transactional
    fun delete(paste: Paste) {
        // pasteTagRepository.deleteByPaste(paste.key)
        // publicPasteEngagementRepository.deleteByPasteId(paste.id!!)

        if (paste.storageType == StorageType.S3) {
            // minioPasteService.delete(paste)
        }

        if (paste.indexedInElastic) {
            // elasticPasteService.delete(paste)
        }

        pasteRepository.delete(paste)

        // pasteContentCache.deleteCachedContent(paste)
    }
}