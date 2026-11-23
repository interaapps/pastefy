package de.interaapps.pastefy.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.core.converter.ModelConverter
import io.swagger.v3.core.jackson.ModelResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration {
    @Bean
    fun openApiModelResolver(objectMapper: ObjectMapper): ModelConverter =
        ModelResolver(objectMapper)
}
