package de.interaapps.pastefy.repositories

import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.PasteComment
import de.interaapps.pastefy.entities.PublicPasteEngagement
import de.interaapps.pastefy.enums.PasteVisibility
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
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

    private fun comment(paste: String, content: String, parentId: Int? = null, createdAt: Instant) =
        PasteComment(paste = paste, userId = "user-001", content = content, parentId = parentId, createdAt = createdAt)

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
