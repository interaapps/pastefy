package de.interaapps.pastefy.controller

import de.interaapps.pastefy.dto.app.AppInfoResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/app")
class AppController {
    @GetMapping("/info")
    fun appInfo(): AppInfoResponse = TODO()
}
