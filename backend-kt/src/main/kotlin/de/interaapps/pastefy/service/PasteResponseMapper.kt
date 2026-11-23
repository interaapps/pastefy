package de.interaapps.pastefy.service

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.dto.pastes.PasteAiInfoResponse
import de.interaapps.pastefy.dto.pastes.PasteResponse
import de.interaapps.pastefy.dto.user.PublicUserDto
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.repositories.PasteAIInfoRepository
import de.interaapps.pastefy.repositories.PasteTagRepository
import de.interaapps.pastefy.repositories.UserRepository
import de.interaapps.pastefy.util.shorten
import org.springframework.stereotype.Service

@Service
class PasteResponseMapper(
    private val pasteService: PasteService,
    private val pasteTagRepository: PasteTagRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val aiInfoRepository: PasteAIInfoRepository,
    private val pasteMetricsService: PasteMetricsService,
    properties: PastefyProperties,
) {
    private val serverName = properties.serverName.trimEnd('/')

    fun map(
        paste: Paste,
        currentUser: User? = null,
        fetchStar: Boolean = false,
        fetchUser: Boolean = false,
        withAiInfo: Boolean = false,
        shortenContent: Boolean = false,
        metrics: PasteMetrics? = null,
    ): PasteResponse {
        val resolvedMetrics = metrics ?: pasteMetricsService.getMetrics(paste.key)
        val content = pasteService.getContent(paste)
            .orEmpty()
            .let { raw ->
                if (shortenContent) {
                    raw.shorten()
                } else {
                    raw
                }
            }
        return PasteResponse(
            exists = true,
            id = paste.key,
            content = content,
            title = paste.title,
            encrypted = paste.encrypted,
            folder = paste.folder,
            userId = paste.userId,
            visibility = paste.visibility ?: PasteVisibility.UNLISTED,
            forkedFrom = paste.forkedFrom,
            rawUrl = "$serverName/${paste.key}/raw",
            type = paste.type ?: PasteType.PASTE,
            createdAt = paste.createdAt?.toString() ?: "0000-00-00 00:00:00",
            expireAt = paste.expireAt?.toString(),
            tags = pasteTagRepository.findAllByPaste(paste.key).map { it.tag },
            user = if (fetchUser) paste.userId?.let(userRepository::findById)?.orElse(null)?.toPublicDto() else null,
            starred = if (fetchStar && currentUser != null) userService.hasStarred(currentUser, paste) else null,
            starCount = resolvedMetrics.starCount,
            commentCount = resolvedMetrics.commentCount,
            viewCount = resolvedMetrics.viewCount,
            aiInfo = if (withAiInfo) paste.id?.let(aiInfoRepository::findById)?.orElse(null)?.let {
                PasteAiInfoResponse(
                    dangerous = it.dangerous,
                    suggestedFilename = it.suggestedFilename,
                    warnings = it.warningsJson,
                    tags = it.tagsJson,
                    description = it.description,
                )
            } else null,
        )
    }
}

fun User.toPublicDto() = PublicUserDto(
    id = id,
    name = uniqueName,
    displayName = name,
    avatar = avatar,
)
