package de.interaapps.pastefy.entities

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "pastefy_paste_ai_info")
class PasteAIInfo(
    @Id var pasteId: Int = 0,
    @Column(nullable = false) var sourcePasteVersion: Int = 0,
    @Column(nullable = false) var promptVersion: Int = 0,
    @Column(length = 30) var provider: String? = null,
    @Column(length = 100) var model: String? = null,
    @Column(length = 500) var description: String? = null,
    @Convert(converter = StringListJsonConverter::class)
    @Column(length = 2048) var tagsJson: MutableList<String>? = null,
    @Convert(converter = AIWarningListJsonConverter::class)
    @Column(length = 4096) var warningsJson: MutableList<AIWarning>? = null,
    @Column(nullable = false) var dangerous: Boolean = false,
    @Column(nullable = false) var maxSeverity: Int = 0,
    @Column(length = 255) var suggestedFilename: String? = null,
    var generatedAt: Instant? = null,
    @Column(nullable = false, updatable = false) var createdAt: Instant? = null,
    @Column(nullable = false) var updatedAt: Instant? = null,
) {
    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        if (createdAt == null) createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}

data class AIWarning(
    val description: String = "",
    val severity: Int = 1,
)

@Converter
class StringListJsonConverter : AttributeConverter<MutableList<String>?, String?> {
    override fun convertToDatabaseColumn(attribute: MutableList<String>?): String? =
        attribute?.let(MAPPER::writeValueAsString)

    override fun convertToEntityAttribute(dbData: String?): MutableList<String>? =
        dbData?.takeIf(String::isNotBlank)
            ?.let { MAPPER.readValue(it, object : TypeReference<MutableList<String>>() {}) }
}

@Converter
class AIWarningListJsonConverter : AttributeConverter<MutableList<AIWarning>?, String?> {
    override fun convertToDatabaseColumn(attribute: MutableList<AIWarning>?): String? =
        attribute?.let(MAPPER::writeValueAsString)

    override fun convertToEntityAttribute(dbData: String?): MutableList<AIWarning>? =
        dbData?.takeIf(String::isNotBlank)
            ?.let { MAPPER.readValue(it, object : TypeReference<MutableList<AIWarning>>() {}) }
}

private val MAPPER = jacksonObjectMapper()
