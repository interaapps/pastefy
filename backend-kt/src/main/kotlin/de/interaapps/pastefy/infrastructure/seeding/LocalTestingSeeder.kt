package de.interaapps.pastefy.infrastructure.seeding

import de.interaapps.pastefy.entities.AIWarning
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.BackgroundJob
import de.interaapps.pastefy.entities.Folder
import de.interaapps.pastefy.entities.Notification
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.entities.PasteAIInfo
import de.interaapps.pastefy.entities.PasteComment
import de.interaapps.pastefy.entities.PasteStar
import de.interaapps.pastefy.entities.PasteTag
import de.interaapps.pastefy.entities.PublicPasteEngagement
import de.interaapps.pastefy.entities.SharedPaste
import de.interaapps.pastefy.entities.TagListing
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.enums.PasteVisibility
import de.interaapps.pastefy.enums.StorageType
import de.interaapps.pastefy.repositories.AuthKeyRepository
import de.interaapps.pastefy.repositories.BackgroundJobRepository
import de.interaapps.pastefy.repositories.FolderRepository
import de.interaapps.pastefy.repositories.NotificationRepository
import de.interaapps.pastefy.repositories.PasteAIInfoRepository
import de.interaapps.pastefy.repositories.PasteCommentRepository
import de.interaapps.pastefy.repositories.PasteRepository
import de.interaapps.pastefy.repositories.PasteStarRepository
import de.interaapps.pastefy.repositories.PasteTagRepository
import de.interaapps.pastefy.repositories.PublicPasteEngagementRepository
import de.interaapps.pastefy.repositories.SharedPasteRepository
import de.interaapps.pastefy.repositories.TagListingRepository
import de.interaapps.pastefy.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
@Profile("!prod")
@ConditionalOnProperty(prefix = "pastefy.seeding", name = ["enabled"], havingValue = "true")
class LocalTestingSeeder(
    private val users: UserRepository,
    private val authKeys: AuthKeyRepository,
    private val folders: FolderRepository,
    private val pastes: PasteRepository,
    private val pasteTags: PasteTagRepository,
    private val pasteStars: PasteStarRepository,
    private val comments: PasteCommentRepository,
    private val notifications: NotificationRepository,
    private val sharedPastes: SharedPasteRepository,
    private val tagListings: TagListingRepository,
    private val engagements: PublicPasteEngagementRepository,
    private val aiInfos: PasteAIInfoRepository,
    private val backgroundJobs: BackgroundJobRepository,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(LocalTestingSeeder::class.java)

    @Transactional
    override fun run(args: ApplicationArguments) {
        val state = SeedState()
        seedUsers(state)
        seedGeneratedUsers()
        seedAuthKeys()
        seedFolders()
        seedGeneratedFolders()
        seedPastes(state)
        seedGeneratedPastes()
        seedPasteMetadata(state)
        seedUserFacingRecords()
        seedOperationalRecords(state)
        log.info("Local Pastefy seed data is ready")
    }

    private fun seedUsers(state: SeedState) {
        state.admin = user(
            id = ADMIN_ID,
            uniqueName = "seed-admin",
            name = "Seed Admin",
            email = "seed-admin@pastefy.local",
            type = User.Type.ADMIN,
            provider = User.AuthenticationProvider.INTERAAPPS,
            authId = "seed-interaapps-admin",
            avatar = "https://pastefy.local/avatars/admin.png",
        )
        state.user = user(
            id = USER_ID,
            uniqueName = "seed-user",
            name = "Seed User",
            email = "seed-user@pastefy.local",
            type = User.Type.USER,
            provider = User.AuthenticationProvider.GITHUB,
            authId = "seed-github-user",
            avatar = "https://pastefy.local/avatars/user.png",
        )
        state.blocked = user(
            id = BLOCKED_ID,
            uniqueName = "seed-blocked",
            name = "Seed Blocked",
            email = "seed-blocked@pastefy.local",
            type = User.Type.BLOCKED,
            provider = User.AuthenticationProvider.GOOGLE,
            authId = "seed-google-blocked",
        )
        state.awaiting = user(
            id = AWAITING_ID,
            uniqueName = "seed-awaiting",
            name = "Seed Awaiting",
            email = "seed-awaiting@pastefy.local",
            type = User.Type.AWAITING_ACCESS,
            provider = User.AuthenticationProvider.DISCORD,
            authId = "seed-discord-awaiting",
        )
    }

    private fun seedGeneratedUsers() {
        val providers = listOf(
            User.AuthenticationProvider.INTERAAPPS,
            User.AuthenticationProvider.GITHUB,
            User.AuthenticationProvider.GOOGLE,
            User.AuthenticationProvider.TWITCH,
            User.AuthenticationProvider.DISCORD,
        )

        repeat(TARGET_USER_COUNT - FIXED_USER_COUNT) { index ->
            val number = index + 1
            user(
                id = generatedUserId(number),
                uniqueName = "seed-user-$number",
                name = "Seed User $number",
                email = "seed-user-$number@pastefy.local",
                type = User.Type.USER,
                provider = providers[index % providers.size],
                authId = "seed-generated-user-$number",
                avatar = "https://pastefy.local/avatars/user-$number.png",
            )
        }
    }

    private fun seedAuthKeys() {
        authKey(
            key = "seed-admin-user-session-key-000000000000000000000000000001",
            userId = ADMIN_ID,
            type = AuthKey.Type.USER,
        )
        authKey(
            key = "seed-user-api-key-000000000000000000000000000000000000001",
            userId = USER_ID,
            type = AuthKey.Type.API,
            scopes = mutableListOf("pastes", "folders", "comments"),
        )
        authKey(
            key = "seed-user-access-token-000000000000000000000000000000001",
            userId = USER_ID,
            type = AuthKey.Type.ACCESS_TOKEN,
            scopes = mutableListOf("pastes:read", "comments:create"),
            accessToken = "seed-access-token",
            refreshToken = "seed-refresh-token",
        )
    }

    private fun seedFolders() {
        folder(ROOT_FOLDER_KEY, "Seed Root Folder", USER_ID)
        folder(CHILD_FOLDER_KEY, "Seed Child Folder", USER_ID, ROOT_FOLDER_KEY)
        folder(ADMIN_FOLDER_KEY, "Admin Examples", ADMIN_ID)
    }

    private fun seedGeneratedFolders() {
        repeat(TARGET_FOLDER_COUNT - FIXED_FOLDER_COUNT) { index ->
            val number = index + 1
            val owner = if (number % 3 == 0) ADMIN_ID else generatedUserId(number)
            folder(
                key = generatedFolderKey(number),
                name = "Generated Folder $number",
                userId = owner,
                parent = if (number % 2 == 0) ROOT_FOLDER_KEY else null,
            )
        }
    }

    private fun seedPastes(state: SeedState) {
        state.publicPaste = paste(
            key = PUBLIC_PASTE_KEY,
            title = "Public Kotlin example",
            content = """
                fun main() {
                    println("Hello from Pastefy seed data")
                }
            """.trimIndent(),
            userId = USER_ID,
            folder = ROOT_FOLDER_KEY,
            visibility = PasteVisibility.PUBLIC,
        )
        state.privatePaste = paste(
            key = PRIVATE_PASTE_KEY,
            title = "Private deployment notes",
            content = "This private paste is useful for testing auth-gated responses.",
            userId = USER_ID,
            folder = CHILD_FOLDER_KEY,
            visibility = PasteVisibility.PRIVATE,
        )
        state.unlistedPaste = paste(
            key = UNLISTED_PASTE_KEY,
            title = "Unlisted JSON fixture",
            content = """{"environment":"local","seeded":true,"items":[1,2,3]}""",
            userId = ADMIN_ID,
            folder = ADMIN_FOLDER_KEY,
            visibility = PasteVisibility.UNLISTED,
        )
        state.encryptedPaste = paste(
            key = ENCRYPTED_PASTE_KEY,
            title = "Encrypted sample",
            content = "ciphertext-placeholder",
            userId = USER_ID,
            visibility = PasteVisibility.PRIVATE,
            encrypted = true,
        )
        state.multiPaste = paste(
            key = MULTI_PASTE_KEY,
            title = "Multi paste manifest",
            content = """[{"name":"README.md","paste":"$PUBLIC_PASTE_KEY"},{"name":"config.json","paste":"$UNLISTED_PASTE_KEY"}]""",
            userId = USER_ID,
            visibility = PasteVisibility.PUBLIC,
            type = PasteType.MULTI_PASTE,
        )
        state.expiringPaste = paste(
            key = EXPIRING_PASTE_KEY,
            title = "Expiring paste",
            content = "This paste has an expiry timestamp for local filtering tests.",
            userId = USER_ID,
            visibility = PasteVisibility.PUBLIC,
            expireAt = Instant.now().plus(7, ChronoUnit.DAYS),
        )
    }

    private fun seedGeneratedPastes() {
        val visibilities = listOf(PasteVisibility.PUBLIC, PasteVisibility.UNLISTED, PasteVisibility.PRIVATE)
        val languages = listOf("kotlin", "json", "markdown", "yaml", "shell", "sql")

        repeat(TARGET_PASTE_COUNT - FIXED_PASTE_COUNT) { index ->
            val number = index + 1
            val language = languages[index % languages.size]
            val key = generatedPasteKey(number)
            val owner = if (number % 5 == 0) ADMIN_ID else generatedUserId(((number - 1) % (TARGET_USER_COUNT - FIXED_USER_COUNT)) + 1)
            val folder = if (number % 4 == 0) null else generatedFolderKey(((number - 1) % (TARGET_FOLDER_COUNT - FIXED_FOLDER_COUNT)) + 1)
            val visibility = visibilities[index % visibilities.size]

            paste(
                key = key,
                title = "Generated $language paste $number",
                content = generatedPasteContent(number, language, visibility),
                userId = owner,
                folder = folder,
                visibility = visibility,
                encrypted = number % 37 == 0,
                type = if (number % 29 == 0) PasteType.MULTI_PASTE else PasteType.PASTE,
                expireAt = if (number % 23 == 0) Instant.now().plus(number.toLong(), ChronoUnit.DAYS) else null,
            )
            tags(key, language, "generated", visibility.name.lowercase())
        }
    }

    private fun seedPasteMetadata(state: SeedState) {
        tags(PUBLIC_PASTE_KEY, "kotlin", "spring", "demo")
        tags(UNLISTED_PASTE_KEY, "json", "fixture")
        tags(MULTI_PASTE_KEY, "multipaste", "demo")

        tagListing("kotlin", "Kotlin", "Spring Boot Kotlin examples", "https://kotlinlang.org", 1)
        tagListing("spring", "Spring", "Spring Boot API examples", "https://spring.io", 1)
        tagListing("json", "JSON", "JSON fixtures for frontend testing", null, 1)
        tagListing("demo", "Demo", "Local seed data", null, 2)

        star(PUBLIC_PASTE_KEY, ADMIN_ID)
        star(PUBLIC_PASTE_KEY, USER_ID)
        star(UNLISTED_PASTE_KEY, USER_ID)

        comment(
            paste = PUBLIC_PASTE_KEY,
            userId = ADMIN_ID,
            content = "Top-level seeded comment for UI tests.",
        )
        comment(
            paste = PUBLIC_PASTE_KEY,
            userId = USER_ID,
            content = "Line-specific seeded comment.",
            lineFrom = 2,
            lineTo = 2,
        )

        state.publicPaste.id?.let { pasteId ->
            engagement(pasteId, 42)
            aiInfo(
                pasteId = pasteId,
                sourceVersion = state.publicPaste.version ?: 1,
                description = "A small Kotlin hello-world example generated for local testing.",
                tags = mutableListOf("kotlin", "spring", "demo"),
            )
        }
    }

    private fun seedUserFacingRecords() {
        notification(USER_ID, "Welcome to the local Pastefy seed dataset.", "/$PUBLIC_PASTE_KEY", received = false)
        notification(USER_ID, "A seeded paste was shared with you.", "/$UNLISTED_PASTE_KEY", received = true)
        notification(ADMIN_ID, "Admin seed account is ready.", "/admin", received = true, alreadyRead = true)

        sharedPaste(userId = ADMIN_ID, targetId = USER_ID, paste = UNLISTED_PASTE_KEY)
    }

    private fun seedOperationalRecords(state: SeedState) {
        state.publicPaste.id?.let { pasteId ->
            backgroundJob(
                key = "seed-paste-ai-info-$pasteId-v${state.publicPaste.version ?: 1}",
                entityId = pasteId,
                sourceVersion = state.publicPaste.version ?: 1,
                promptVersion = 1,
                status = BackgroundJob.Status.DONE,
            )
        }
    }

    private fun user(
        id: String,
        uniqueName: String,
        name: String,
        email: String,
        type: User.Type,
        provider: User.AuthenticationProvider,
        authId: String,
        avatar: String? = null,
    ): User =
        users.findById(id).orElseGet {
            users.save(
                User(
                    id = id,
                    name = name,
                    uniqueName = uniqueName,
                    email = email,
                    avatar = avatar,
                    authId = authId,
                    authProvider = provider,
                    type = type,
                )
            )
        }

    private fun authKey(
        key: String,
        userId: String,
        type: AuthKey.Type,
        scopes: MutableList<String>? = null,
        accessToken: String? = null,
        refreshToken: String? = null,
    ) {
        if (authKeys.findByKey(key) != null) return
        authKeys.save(
            AuthKey(
                key = key,
                userId = userId,
                type = type,
                scopes = scopes,
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        )
    }

    private fun folder(key: String, name: String, userId: String, parent: String? = null): Folder =
        folders.findByKey(key) ?: folders.save(Folder(key = key, name = name, userId = userId, parent = parent))

    private fun paste(
        key: String,
        title: String,
        content: String,
        userId: String,
        visibility: PasteVisibility,
        folder: String? = null,
        encrypted: Boolean = false,
        type: PasteType = PasteType.PASTE,
        expireAt: Instant? = null,
    ): Paste {
        val existing = pastes.findByKey(key)
        if (existing != null) return existing
        return pastes.save(
            Paste(
                key = key,
                title = title,
                userId = userId,
                folder = folder,
                encrypted = encrypted,
                type = type,
                visibility = visibility,
                storageType = StorageType.DATABASE,
                expireAt = expireAt,
            ).apply { setDatabaseContent(content) }
        )
    }

    private fun tags(paste: String, vararg tags: String) {
        val existing = pasteTags.findAllByPaste(paste).mapTo(mutableSetOf()) { it.tag }
        tags.filterNot(existing::contains).forEach { tag ->
            pasteTags.save(PasteTag(paste = paste, tag = tag))
        }
    }

    private fun tagListing(tag: String, displayName: String, description: String, website: String?, pasteCount: Int) {
        if (tagListings.existsById(tag)) return
        tagListings.save(
            TagListing(
                tag = tag,
                displayName = displayName,
                description = description,
                website = website,
                icon = "tag",
                pasteCount = pasteCount,
            )
        )
    }

    private fun star(paste: String, userId: String) {
        if (pasteStars.existsByPasteAndUserId(paste, userId)) return
        pasteStars.save(PasteStar(paste = paste, userId = userId))
    }

    private fun comment(paste: String, userId: String, content: String, lineFrom: Int? = null, lineTo: Int? = null) {
        if (comments.findAllByPaste(paste).any { it.content == content && it.userId == userId }) return
        comments.save(PasteComment(paste = paste, userId = userId, content = content, lineFrom = lineFrom, lineTo = lineTo))
    }

    private fun notification(
        userId: String,
        message: String,
        url: String,
        received: Boolean,
        alreadyRead: Boolean = false,
    ) {
        if (notifications.findAllByUserId(userId).any { it.message == message }) return
        notifications.save(
            Notification(
                userId = userId,
                message = message,
                url = url,
                received = received,
                alreadyRead = alreadyRead,
            )
        )
    }

    private fun sharedPaste(userId: String, targetId: String, paste: String) {
        if (sharedPastes.findAllByTargetId(targetId).any { it.userId == userId && it.paste == paste }) return
        sharedPastes.save(SharedPaste(userId = userId, targetId = targetId, paste = paste))
    }

    private fun engagement(pasteId: Int, score: Int) {
        if (engagements.findByPasteId(pasteId) != null) return
        engagements.save(PublicPasteEngagement(pasteId = pasteId, score = score))
    }

    private fun aiInfo(
        pasteId: Int,
        sourceVersion: Int,
        description: String,
        tags: MutableList<String>,
    ) {
        if (aiInfos.existsById(pasteId)) return
        aiInfos.save(
            PasteAIInfo(
                pasteId = pasteId,
                sourcePasteVersion = sourceVersion,
                promptVersion = 1,
                provider = "seed",
                model = "local-fixture",
                description = description,
                tagsJson = tags,
                warningsJson = mutableListOf(AIWarning("No issue detected in seed data.", 1)),
                dangerous = false,
                maxSeverity = 1,
                suggestedFilename = "hello.kt",
                generatedAt = Instant.now(),
            )
        )
    }

    private fun generatedPasteContent(number: Int, language: String, visibility: PasteVisibility): String =
        when (language) {
            "kotlin" -> """
                data class SeedPaste$number(
                    val id: Int = $number,
                    val visibility: String = "${visibility.name}"
                )
            """.trimIndent()
            "json" -> """{"id":$number,"kind":"generated","visibility":"${visibility.name.lowercase()}"}"""
            "markdown" -> "# Generated Paste $number\n\nThis is seeded markdown content for local testing."
            "yaml" -> "id: $number\nkind: generated\nvisibility: ${visibility.name.lowercase()}\n"
            "shell" -> "echo \"Generated Paste $number\"\n"
            "sql" -> "select $number as generated_paste_id;\n"
            else -> "Generated paste $number"
        }

    private fun generatedUserId(number: Int): String = "seedu%03d".format(number)

    private fun generatedFolderKey(number: Int): String = "seedf%03d".format(number)

    private fun generatedPasteKey(number: Int): String = "seedp%03d".format(number)

    private fun backgroundJob(
        key: String,
        entityId: Int,
        sourceVersion: Int,
        promptVersion: Int,
        status: BackgroundJob.Status,
    ) {
        if (backgroundJobs.existsById(key)) return
        backgroundJobs.save(
            BackgroundJob(
                key = key,
                entityId = entityId,
                sourceVersion = sourceVersion,
                promptVersion = promptVersion,
                status = status,
                attempts = 1,
                availableAt = Instant.now(),
            )
        )
    }

    private class SeedState {
        lateinit var admin: User
        lateinit var user: User
        lateinit var blocked: User
        lateinit var awaiting: User
        lateinit var publicPaste: Paste
        lateinit var privatePaste: Paste
        lateinit var unlistedPaste: Paste
        lateinit var encryptedPaste: Paste
        lateinit var multiPaste: Paste
        lateinit var expiringPaste: Paste
    }

    companion object {
        private const val ADMIN_ID = "seedadm1"
        private const val USER_ID = "seedusr1"
        private const val BLOCKED_ID = "seedblk1"
        private const val AWAITING_ID = "seedawt1"

        private const val ROOT_FOLDER_KEY = "seedroot"
        private const val CHILD_FOLDER_KEY = "seedchld"
        private const val ADMIN_FOLDER_KEY = "seedadmf"

        private const val PUBLIC_PASTE_KEY = "seedpub1"
        private const val PRIVATE_PASTE_KEY = "seedpriv"
        private const val UNLISTED_PASTE_KEY = "seedunls"
        private const val ENCRYPTED_PASTE_KEY = "seedencr"
        private const val MULTI_PASTE_KEY = "seedmult"
        private const val EXPIRING_PASTE_KEY = "seedexpr"

        private const val TARGET_USER_COUNT = 50
        private const val FIXED_USER_COUNT = 4
        private const val TARGET_FOLDER_COUNT = 10
        private const val FIXED_FOLDER_COUNT = 3
        private const val TARGET_PASTE_COUNT = 200
        private const val FIXED_PASTE_COUNT = 6
    }
}
