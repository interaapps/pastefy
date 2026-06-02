package de.interaapps.pastefy.controller.stats

import de.interaapps.pastefy.auth.annotations.CurrentAuthKey
import de.interaapps.pastefy.auth.annotations.CurrentUser
import de.interaapps.pastefy.dto.app.StatsResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/app/stats")
class StatsController {
    @GetMapping
    fun stats(@CurrentAuthKey authKey: AuthKey?, @CurrentUser user: User?): StatsResponse = TODO()
}
