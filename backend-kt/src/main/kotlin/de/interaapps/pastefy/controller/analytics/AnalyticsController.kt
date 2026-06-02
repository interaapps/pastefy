package de.interaapps.pastefy.controller.analytics

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.analytics.AnalyticsResponse
import de.interaapps.pastefy.dto.analytics.AnalyticsQuery
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletRequest
import de.interaapps.pastefy.exceptions.FeatureDisabledException
import de.interaapps.pastefy.exceptions.PermissionsDeniedException
import de.interaapps.pastefy.infrastructure.analytics.AnalyticsService
import de.interaapps.pastefy.service.PasteService
import org.springframework.beans.factory.ObjectProvider
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/analytics")
@Authenticated
class AnalyticsController(
    private val analyticsProvider: ObjectProvider<AnalyticsService>,
    private val pasteService: PasteService,
) {
    @GetMapping("/admin")
    @AdminRoute
    @RequiresPermission("analytics:read")
    fun admin(request: HttpServletRequest, @CurrentAuthKey authKey: AuthKey): AnalyticsResponse = query(AnalyticsQuery.from(request))

    @GetMapping("/pastes/{id}")
    @RequiresPermission("analytics:read")
    fun paste(@PathVariable id: String, request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): AnalyticsResponse {
        val paste = pasteService.get(id)
        if (paste == null || (!user.isAdmin && user.id != paste.userId)) throw PermissionsDeniedException()
        return query(AnalyticsQuery.from(request).apply { filters["paste_key"] = paste.key })
    }

    @GetMapping("/user")
    @RequiresPermission("analytics:read")
    fun user(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): AnalyticsResponse =
        query(AnalyticsQuery.from(request).apply { filters["paste_user_id"] = user.id })

    private fun query(query: AnalyticsQuery): AnalyticsResponse =
        (analyticsProvider.ifAvailable ?: throw FeatureDisabledException()).query(query)
}
