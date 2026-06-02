package de.interaapps.pastefy.controller.user

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.ActionResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.Notification
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/user/notification")
@Authenticated
class NotificationController {
    @PostMapping
    @RequiresPermission("notifications:create")
    fun add(@CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()

    @GetMapping
    @RequiresPermission("notifications:read")
    fun getNotifications(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<Notification> = TODO()

    @GetMapping("/readall")
    @RequiresPermission("notifications:edit")
    fun readAll(@CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()
}
