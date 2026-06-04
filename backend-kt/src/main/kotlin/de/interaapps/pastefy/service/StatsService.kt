package de.interaapps.pastefy.service

import de.interaapps.pastefy.dto.app.StatsResponse
import de.interaapps.pastefy.enums.StorageType
import de.interaapps.pastefy.infrastructure.elastic.ElasticPasteService
import de.interaapps.pastefy.repositories.FolderRepository
import de.interaapps.pastefy.repositories.PasteRepository
import de.interaapps.pastefy.repositories.TagListingRepository
import de.interaapps.pastefy.repositories.UserRepository
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service

@Service
class StatsService(
    private val pasteRepository: PasteRepository,
    private val userRepository: UserRepository,
    private val tagListingRepository: TagListingRepository,
    private val folderRepository: FolderRepository,
    private val elasticProvider: ObjectProvider<ElasticPasteService>,
) {
    fun get(): StatsResponse = StatsResponse(
        createdPastes = pasteRepository.count().toInt(),
        loggedInPastes = pasteRepository.countByUserIdIsNotNull().toInt(),
        userCount = userRepository.count().toInt(),
        tagCount = tagListingRepository.count().toInt(),
        folderCount = folderRepository.count().toInt(),
        indexedPastes = elasticProvider.ifAvailable?.count(),
        s3Pastes = pasteRepository.countByStorageType(StorageType.S3).toInt(),
    )
}
