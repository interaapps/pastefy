package de.interaapps.pastefy.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.PasteComment
import de.interaapps.pastefy.entities.PasteStar
import de.interaapps.pastefy.entities.PublicPasteEngagement
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.service.query.JpaPasteQueryAdapter
import de.interaapps.pastefy.service.query.LegacyPasteQueryParser
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant

@DataJpaTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.liquibase.enabled=false",
    ],
)
@Testcontainers(disabledWithoutDocker = true)
class PasteCommentRepositoryIntegrationTest(
    @Autowired private val comments: PasteCommentRepository,
    @Autowired private val pastes: PasteRepository,
    @Autowired private val engagements: PublicPasteEngagementRepository,
    @Autowired private val authKeys: AuthKeyRepository,
    @Autowired private val stars: PasteStarRepository,
    @Autowired private val entityManager: EntityManager,
) {
    @Test
    fun `loads top level comments newest first and replies oldest first`() {
        val root = comments.save(comment("paste-01", "root", createdAt = Instant.parse("2026-01-01T00:00:00Z")))
        comments.save(comment("paste-01", "reply-2", parentId = root.id, createdAt = Instant.parse("2026-01-03T00:00:00Z")))
        comments.save(comment("paste-01", "reply-1", parentId = root.id, createdAt = Instant.parse("2026-01-02T00:00:00Z")))

        assertEquals(
            listOf("root"),
            comments.findAllByPasteAndParentIdIsNullOrderByCreatedAtDesc("paste-01", PageRequest.of(0, 10)).map { it.content },
        )
        assertEquals(
            listOf("reply-1", "reply-2"),
            comments.findAllByParentIdOrderByCreatedAtAsc(requireNotNull(root.id)).map { it.content },
        )
    }

    @Test
    fun `orders trending public pastes by engagement score`() {
        val low = pastes.save(
            Paste(key = "trend001", title = "low", visibility = PasteVisibility.PUBLIC).apply {
                setDatabaseContent("low")
            },
        )
        val high = pastes.save(
            Paste(key = "trend002", title = "high", visibility = PasteVisibility.PUBLIC).apply {
                setDatabaseContent("high")
            },
        )
        engagements.save(PublicPasteEngagement(pasteId = requireNotNull(low.id), score = 1))
        engagements.save(PublicPasteEngagement(pasteId = requireNotNull(high.id), score = 9))

        assertEquals(
            listOf("trend002", "trend001"),
            pastes.findTrending(null, PageRequest.of(0, 10)).map(Paste::key),
        )
    }

    @Test
    fun `loads api keys by user and type`() {
        authKeys.save(AuthKey(key = "api-key", userId = "user-001", type = AuthKey.Type.API))

        assertEquals(
            listOf("api-key"),
            authKeys.findAllByUserIdAndType("user-001", AuthKey.Type.API).map(AuthKey::key),
        )
    }

    @Test
    fun `legacy paste query filters with bracket parameters`() {
        pastes.save(paste("query001", "Kotlin Public", PasteVisibility.PUBLIC))
        pastes.save(paste("query002", "Kotlin Private", PasteVisibility.PRIVATE))
        entityManager.flush()

        val request = MockHttpServletRequest().apply {
            addParameter("search", "kotlin")
            addParameter("filter[visibility]", "PUBLIC")
        }

        assertEquals(
            listOf("query001"),
            queryAdapter().find(parser().parse(request, MockHttpServletResponse(), user = null, guarded = false)).map(Paste::key),
        )
    }

    @Test
    fun `legacy paste query supports starredBy filters json`() {
        val paste = pastes.save(paste("query003", "Starred Paste", PasteVisibility.UNLISTED))
        stars.save(PasteStar(paste = paste.key, userId = "user-001"))
        entityManager.flush()

        val request = MockHttpServletRequest().apply {
            addParameter("filters", """{"starredBy":"user-001"}""")
        }

        assertEquals(
            listOf("query003"),
            queryAdapter().find(parser().parse(request, MockHttpServletResponse(), user = null, guarded = false)).map(Paste::key),
        )
    }

    private fun comment(paste: String, content: String, parentId: Int? = null, createdAt: Instant) =
        PasteComment(paste = paste, userId = "user-001", content = content, parentId = parentId, createdAt = createdAt)

    private fun paste(key: String, title: String, visibility: PasteVisibility): Paste =
        Paste(key = key, title = title, visibility = visibility).apply { setDatabaseContent(title.lowercase()) }

    private fun parser() = LegacyPasteQueryParser(ObjectMapper(), PastefyProperties())

    private fun queryAdapter() = JpaPasteQueryAdapter(entityManager)

    companion object {
        @Container
        @JvmStatic
        val mysql = MySQLContainer<Nothing>("mysql:8.4")

        @DynamicPropertySource
        @JvmStatic
        fun datasource(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", mysql::getJdbcUrl)
            registry.add("spring.datasource.username", mysql::getUsername)
            registry.add("spring.datasource.password", mysql::getPassword)
            registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName)
        }
    }
}
