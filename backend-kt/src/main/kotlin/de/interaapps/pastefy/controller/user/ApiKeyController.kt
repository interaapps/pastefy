package de.interaapps.pastefy.controller.user

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.ActionResponse
import de.interaapps.pastefy.dto.user.CreateAuthKeyResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.repositories.AuthKeyRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/user/keys")
@Authenticated
class ApiKeyController(
    private val repository: AuthKeyRepository,
) {
    @PostMapping
    @RequiresPermission("authkeys:create")
    fun addKey(@CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): CreateAuthKeyResponse {
        val created = repository.save(AuthKey(userId = user.id, type = AuthKey.Type.API))
        return CreateAuthKeyResponse(key = created.key, success = true)
    }

    @GetMapping
    @RequiresPermission("authkeys:read")
    fun getKeys(@CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<String> =
        repository.findAllByUserIdAndType(user.id, AuthKey.Type.API).map(AuthKey::key)

    @DeleteMapping("/{key}")
    @RequiresPermission("authkeys:delete")
    fun deleteKey(@PathVariable key: String, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): ActionResponse {
        repository.deleteByKeyAndUserId(key, user.id)
        return ActionResponse(success = true)
    }
}
