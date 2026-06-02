package de.interaapps.pastefy.controller.admin

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.ActionResponse
import de.interaapps.pastefy.dto.user.EditUserRequest
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/admin")
@AdminRoute
class AdminController {
    @GetMapping("/users")
    @RequiresPermission("admin.users:read")
    fun getUsers(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<User> = TODO()

    @GetMapping("/users/{id}")
    @RequiresPermission("admin.users:read")
    fun getUser(@PathVariable id: String, @CurrentAuthKey authKey: AuthKey): User = TODO()

    @DeleteMapping("/users/{id}")
    @RequiresPermission("admin.users:delete")
    fun removeUser(@PathVariable id: String, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()

    @PutMapping("/users/{id}")
    @RequiresPermission("admin.users:edit")
    fun editUser(@PathVariable id: String, @Valid @RequestBody request: EditUserRequest, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()
}
