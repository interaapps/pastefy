package de.interaapps.pastefy.infrastructure.ai

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class AiEnabledCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val environment = context.environment
        val explicitlyEnabled = environment.getProperty("pastefy.ai.enabled", Boolean::class.java, false)
        val hasLegacyConfig = environment.hasText("AI_PROVIDER") ||
                environment.hasText("AI_ANTHROPIC_TOKEN") ||
                environment.hasText("AI_GOOGLE_TOKEN")
        if (!explicitlyEnabled && !hasLegacyConfig) return false

        val provider = environment.getProperty("pastefy.ai.provider").orEmpty().lowercase()
        return when (provider) {
            "anthropic" -> environment.hasText("AI_ANTHROPIC_TOKEN") ||
                    environment.hasText("spring.ai.anthropic.api-key")
            "google", "google-genai" -> environment.hasText("AI_GOOGLE_TOKEN") ||
                    environment.hasText("spring.ai.google.genai.api-key")
            else -> environment.hasText("SPRING_AI_MODEL_CHAT")
        }
    }

    private fun org.springframework.core.env.Environment.hasText(name: String): Boolean =
        getProperty(name)?.isNotBlank() == true
}
