package de.interaapps.pastefy.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

class AiEnvironmentPostProcessor : EnvironmentPostProcessor, Ordered {
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        if (environment.hasText("SPRING_AI_MODEL_CHAT")) return

        val provider = environment.getProperty("AI_PROVIDER").orEmpty().trim().lowercase()
        val chatModel = when {
            provider == "anthropic" -> "anthropic"
            provider == "google" || provider == "google-genai" -> "google-genai"
            environment.hasText("AI_ANTHROPIC_TOKEN") -> "anthropic"
            environment.hasText("AI_GOOGLE_TOKEN") -> "google-genai"
            else -> return
        }

        environment.propertySources.addFirst(
            MapPropertySource("pastefy-ai-env-compat", mapOf("spring.ai.model.chat" to chatModel))
        )
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 10

    private fun org.springframework.core.env.Environment.hasText(name: String): Boolean =
        getProperty(name)?.isNotBlank() == true
}
