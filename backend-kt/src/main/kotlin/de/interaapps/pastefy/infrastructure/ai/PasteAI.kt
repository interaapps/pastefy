package de.interaapps.pastefy.infrastructure.ai

import com.fasterxml.jackson.databind.ObjectMapper
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.entities.Paste
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "pastefy.ai", name = ["enabled"], havingValue = "true")
class PasteAI(
    chatModel: ChatModel,
    private val objectMapper: ObjectMapper,
    properties: PastefyProperties,
) {
    private val client = ChatClient.create(chatModel)
    val provider: String = properties.ai.provider.trim().ifBlank { "spring-ai" }
    val model: String = properties.ai.model.trim().ifBlank { "configured-default" }

    fun generateInfo(paste: Paste, content: String): GeneratedPasteInfo {
        val response = generate(
            """
            You are a metadata extraction service for Pastefy, a public paste and code sharing platform.
            Analyze the paste title and content preview. Return useful metadata for search, discovery, moderation and SEO.
            Return valid JSON only. Do not include markdown or explanations. Do not invent facts.
            Do not include secrets, tokens, passwords, API keys or private data in the description.
            Description must be neutral and at most 500 characters. Tags must be lowercase slugs using only a-z, 0-9 and hyphen.
            Return at most 8 tags. Include exactly one lang-{language} tag if a language is identifiable.
            dangerous is true only for obviously harmful code, malware, credential theft, destructive commands, phishing, token stealing or exploit logic.
            Severity ranges from 1 for harmless to 10 for severe malware, credential theft or destructive code.
            JSON schema:
            {"tags":["string"],"description":"string","system_warnings":[{"description":"string","severity":1}],"suggested_filename":"string","dangerous":false}
            Omit optional fields when they are not needed.
            """.trimIndent(),
            "Title: ${paste.title.orEmpty()}\n\nContent preview:\n${content.take(1_000)}",
        )
        return objectMapper.readValue(jsonObject(response), GeneratedPasteInfo::class.java)
    }

    private fun generate(systemPrompt: String, userPrompt: String): String =
        requireNotNull(
            client.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content(),
        ) { "Spring AI returned an empty response" }

    private fun jsonObject(value: String): String {
        val start = value.indexOf('{')
        val end = value.lastIndexOf('}')
        require(start >= 0 && end > start) { "AI response does not contain a JSON object" }
        val json = value.substring(start, end + 1)
        require(objectMapper.readTree(json).isObject) { "AI response is not a JSON object" }
        return json
    }
}

data class GeneratedPasteInfo(
    val tags: List<String> = emptyList(),
    val description: String = "",
    val systemWarnings: List<GeneratedWarning> = emptyList(),
    val suggestedFilename: String? = null,
    val dangerous: Boolean = false,
)

data class GeneratedWarning(
    val description: String = "",
    val severity: Int = 1,
)
