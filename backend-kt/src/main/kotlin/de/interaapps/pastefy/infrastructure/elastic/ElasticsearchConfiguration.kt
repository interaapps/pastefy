package de.interaapps.pastefy.infrastructure.elastic

import de.interaapps.pastefy.config.PastefyProperties
import org.apache.http.message.BasicHeader
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(prefix = "pastefy.elasticsearch", name = ["enabled"], havingValue = "true")
class ElasticsearchConfiguration {
    @Bean
    fun elasticsearchApiKeyCustomizer(properties: PastefyProperties): RestClientBuilderCustomizer =
        RestClientBuilderCustomizer { builder ->
            val elastic = properties.elasticsearch
            if (elastic.apiKey.isNotBlank()) {
                builder.setDefaultHeaders(arrayOf(BasicHeader("Authorization", "ApiKey ${elastic.apiKey}")))
            }
        }
}
