package de.interaapps.pastefy.infrastructure.s3

import de.interaapps.pastefy.config.PastefyProperties
import io.minio.MinioClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(prefix = "pastefy.s3", name = ["enabled"], havingValue = "true")
class S3Configuration {
    @Bean
    fun minioClient(properties: PastefyProperties): MinioClient {
        val s3 = properties.s3
        require(s3.endpoint.isNotBlank()) { "pastefy.s3.endpoint must be configured when S3 is enabled" }
        require(s3.accessKey.isNotBlank()) { "pastefy.s3.access-key must be configured when S3 is enabled" }
        require(s3.secretKey.isNotBlank()) { "pastefy.s3.secret-key must be configured when S3 is enabled" }
        require(s3.bucket.isNotBlank()) { "pastefy.s3.bucket must be configured when S3 is enabled" }
        return MinioClient.builder()
            .endpoint(s3.endpoint)
            .credentials(s3.accessKey, s3.secretKey)
            .apply {
                s3.region?.takeIf { it.isNotBlank() }?.let(::region)
            }
            .build()
    }
}
