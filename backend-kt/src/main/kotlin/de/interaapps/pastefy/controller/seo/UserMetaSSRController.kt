package de.interaapps.pastefy.controller.seo

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.repositories.PasteRepository
import de.interaapps.pastefy.service.FrontendIndexService
import de.interaapps.pastefy.service.SeoPageCacheService
import de.interaapps.pastefy.service.SeoPageContentService
import de.interaapps.pastefy.service.SeoRenderer
import de.interaapps.pastefy.service.UserService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class UserMetaSSRController(
    private val properties: PastefyProperties,
    private val users: UserService,
    private val pastes: PasteRepository,
    private val seo: SeoRenderer,
    private val seoCache: SeoPageCacheService,
    private val seoContent: SeoPageContentService,
    private val frontendIndex: FrontendIndexService,
) {
    @GetMapping("/@{name}")
    fun userMeta(@PathVariable name: String): ResponseEntity<String> {
        if (!properties.publicPastesEnabled || name.isBlank()) return frontendIndex.frontend()

        val user = users.getByName(name) ?: return frontendIndex.frontend()
        val username = seo.normalizeText(user.uniqueName, name)
        val displayName = seo.normalizeText(user.name, username)
        val title = "$displayName | Pastefy"
        val description = seo.truncate(
            "View @$username on Pastefy and browse public pastes shared by $displayName.",
            180,
        )
        val publicPastes = pastes.findAllByUserIdAndVisibilityAndEncryptedFalseOrderByCreatedAtDesc(
            user.id,
            PasteVisibility.PUBLIC,
            PageRequest.of(0, 9),
        )

        val page = seo.page("/@${seo.pathSegment(username)}", title, description)
            .type("profile")
            .image(user.avatar)
            .meta("author", "$displayName (@$username)")
            .openGraph("profile:username", username)
            .content(
                seo.mainContent(
                    seo.heading(1, displayName),
                    seo.paragraph("@$username"),
                    seo.definitionList(
                        buildMap {
                            put("User id", user.id)
                            user.createdAt?.let { put("Joined", it.toString()) }
                        },
                    ),
                    seoContent.pasteListSection(
                        "Public pastes",
                        publicPastes,
                        "This user has no public pastes yet.",
                    ),
                    seo.link(
                        seo.absoluteUrl("/search?search=%40${seo.pathSegment(username)}&scope=public&page=1"),
                        "Search this user's public pastes",
                    ),
                ),
            )

        return seoCache.renderResponse("user:$username", page) { frontendIndex.frontend() }
    }
}
