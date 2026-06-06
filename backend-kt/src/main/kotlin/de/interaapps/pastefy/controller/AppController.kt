package de.interaapps.pastefy.controller

import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.dto.app.AppInfoResponse
import de.interaapps.pastefy.infrastructure.ai.PasteAI
import de.interaapps.pastefy.infrastructure.analytics.AnalyticsService
import org.springframework.beans.factory.ObjectProvider
import org.springframework.cache.annotation.Cacheable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/app")
class AppController(
    private val properties: PastefyProperties,
    private val aiProvider: ObjectProvider<PasteAI>,
    private val analyticsProvider: ObjectProvider<AnalyticsService>,
) {
    @GetMapping("/info")
    @Cacheable("app-info")
    fun appInfo(): AppInfoResponse = AppInfoResponse(
        customLogo = properties.customLogo?.takeIf(String::isNotBlank),
        customName = properties.customName?.takeIf(String::isNotBlank),
        aiEnabled = aiProvider.ifAvailable != null,
        analyticsEnabled = analyticsProvider.ifAvailable != null,
        customFooter = properties.customFooter.split(',').mapNotNull {
            val parts = it.split('=', limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap(),
        loginRequiredForRead = properties.loginRequiredRead,
        loginRequiredForCreate = properties.loginRequiredCreate,
        encryptionIsDefault = properties.encryptionDefault,
        publicPastesEnabled = properties.publicPastesEnabled,
    )
}
