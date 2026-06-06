package de.interaapps.pastefy.controller.auth

import de.interaapps.pastefy.dto.auth.InteraAppsExternalAccessRequest
import de.interaapps.pastefy.service.InteraAppsExternalAccessService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/auth")
class InteraAppsExternalAccessController(
    private val externalAccessService: InteraAppsExternalAccessService,
) {
    @PostMapping("/iaea")
    fun externalAccess(@Valid @RequestBody request: InteraAppsExternalAccessRequest): String =
        externalAccessService.issue(request)
}
