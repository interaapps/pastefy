package de.interaapps.pastefy.infrastructure.elastic

import de.interaapps.pastefy.config.PastefyProperties
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.message.BasicHeader
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
@ConditionalOnProperty(prefix = "pastefy.elasticsearch", name = ["enabled"], havingValue = "true")
class ElasticsearchConfiguration {
    @Bean
    fun elasticsearchApiKeyCustomizer(
        properties: PastefyProperties,
        environment: Environment,
    ): RestClientBuilderCustomizer =
        RestClientBuilderCustomizer { builder ->
            val elastic = properties.elasticsearch
            val pool = elastic.pool
            if (elastic.apiKey.isNotBlank()) {
                builder.setDefaultHeaders(arrayOf(BasicHeader("Authorization", "ApiKey ${elastic.apiKey}")))
            }
            val username = environment.getProperty("spring.elasticsearch.username", "")
            val password = environment.getProperty("spring.elasticsearch.password", "")
            builder.setHttpClientConfigCallback { httpClientBuilder ->
                if (elastic.apiKey.isBlank() && username.isNotBlank()) {
                    val credentialsProvider = BasicCredentialsProvider().apply {
                        setCredentials(AuthScope.ANY, UsernamePasswordCredentials(username, password))
                    }
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                }
                httpClientBuilder
                    .setMaxConnTotal(pool.maxConnections.coerceAtLeast(1))
                    .setMaxConnPerRoute(pool.maxConnectionsPerRoute.coerceAtLeast(1))
            }
        }
}
