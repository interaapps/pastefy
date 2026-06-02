package de.interaapps.pastefy.controller.user

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.ActionResponse
import de.interaapps.pastefy.dto.user.CreateAuthKeyResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/user/keys")
@Authenticated
class ApiKeyController {
    @PostMapping
    @RequiresPermission("authkeys:create")
    fun addKey(@CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): CreateAuthKeyResponse = TODO()

    @GetMapping
    @RequiresPermission("authkeys:read")
    fun getKeys(@CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<String> = TODO()

    @DeleteMapping("/{key}")
    @RequiresPermission("authkeys:delete")
    fun deleteKey(@PathVariable key: String, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse = TODO()
}
