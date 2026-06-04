package de.interaapps.pastefy.service

import de.interaapps.pastefy.config.PastefyProperties
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class FrontendIndexService(
    private val properties: PastefyProperties,
) {
    val html: String? = load()

    private fun load(): String? = runCatching {
        val source = ClassPathResource("static/index.html").inputStream.bufferedReader().use { it.readText() }
        source
            .replace("/*PASTEFY_PLUGINS*/", "")
            .replace("<!--CUSTOM_HEADERS-->", properties.customHeader)
            .replace("<!--CUSTOM_BODY-->", properties.customBody)
    }.onFailure {
        LOGGER.warn("Unable to load static/index.html for frontend serving", it)
    }.getOrNull()

    companion object {
        private val LOGGER = LoggerFactory.getLogger(FrontendIndexService::class.java)
    }
}
