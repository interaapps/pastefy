package de.interaapps.pastefy.infrastructure.s3

import com.fasterxml.jackson.databind.ObjectMapper
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.entities.Paste
import de.interaapps.pastefy.enums.StorageType
import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

@Service
@ConditionalOnProperty(prefix = "pastefy.s3", name = ["enabled"], havingValue = "true")
class S3PasteService(
    private val client: MinioClient,
    private val objectMapper: ObjectMapper,
    private val properties: PastefyProperties,
) {
    fun shouldStore(paste: Paste): Boolean {
        val threshold = properties.s3.pasteSizeThreshold
        return paste.storageType == StorageType.S3 || threshold >= 0 && (paste.content?.length ?: 0) > threshold
    }

    fun store(paste: Paste, content: String): S3PasteReference {
        val bytes = content.toByteArray(StandardCharsets.UTF_8)
        val response = client.putObject(
            PutObjectArgs.builder()
                .bucket(properties.s3.bucket)
                .`object`(objectName(paste))
                .contentType("text/plain; charset=utf-8")
                .tags(mapOf("userId" to (paste.userId ?: "anonymous"), "paste" to paste.key))
                .stream(ByteArrayInputStream(bytes), bytes.size.toLong(), -1)
                .build()
        )
        return S3PasteReference(
            server = properties.s3.endpoint,
            bucket = response.bucket(),
            objectName = response.`object`(),
            region = response.region() ?: properties.s3.region,
            etag = response.etag(),
        )
    }

    fun getContent(paste: Paste): String {
        val reference = reference(paste)
        return client.getObject(
            GetObjectArgs.builder()
                .bucket(reference.bucket)
                .region(reference.region)
                .`object`(reference.objectName)
                .build()
        ).use { it.readBytes().toString(StandardCharsets.UTF_8) }
    }

    fun delete(paste: Paste) {
        val reference = reference(paste)
        client.removeObject(
            RemoveObjectArgs.builder()
                .bucket(reference.bucket)
                .region(reference.region)
                .`object`(reference.objectName)
                .build()
        )
    }

    fun encode(reference: S3PasteReference): String = objectMapper.writeValueAsString(reference)

    private fun reference(paste: Paste): S3PasteReference {
        require(paste.storageType == StorageType.S3) { "Paste ${paste.id} is not stored in S3" }
        return objectMapper.readValue(requireNotNull(paste.rawContent), S3PasteReference::class.java)
    }

    private fun objectName(paste: Paste): String {
        val prefix = paste.key.takeIf { it.length >= 4 }?.let { "${it.substring(0, 2)}/${it.substring(2, 4)}/" }.orEmpty()
        return "pastes/${paste.userId ?: "anonymous"}/$prefix${paste.key}/contents.txt"
    }
}

data class S3PasteReference(
    val objectName: String,
    val bucket: String,
    val server: String,
    val region: String? = null,
    val etag: String? = null,
)
