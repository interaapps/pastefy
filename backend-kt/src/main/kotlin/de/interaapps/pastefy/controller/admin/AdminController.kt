package de.interaapps.pastefy.controller.admin

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.ActionResponse
import de.interaapps.pastefy.dto.user.EditUserRequest
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import de.interaapps.pastefy.exceptions.NotFoundException
import de.interaapps.pastefy.repositories.UserRepository
import de.interaapps.pastefy.service.UserService
import de.interaapps.pastefy.service.query.LegacyFilterSpecificationBuilder
import de.interaapps.pastefy.service.query.LegacyFilterSpecificationBuilder.Companion.USER_FIELDS
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v2/admin")
@AdminRoute
@Authenticated
class AdminController(
    private val repository: UserRepository,
    private val users: UserService,
    private val legacyFilters: LegacyFilterSpecificationBuilder,
) {
    @GetMapping("/users")
    @RequiresPermission("admin.users:read")
    fun getUsers(request: HttpServletRequest, @CurrentUser user: User, @CurrentAuthKey authKey: AuthKey): List<User> {
        val page = request.getParameter("page")?.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val limit = request.getParameter("page_limit")?.toIntOrNull()?.coerceIn(1, 100) ?: 10
        val search = request.getParameter("search")?.trim()?.lowercase()?.takeIf(String::isNotEmpty)
        val searchSpecification = Specification<User> { root, _, builder ->
            search?.let {
                builder.or(
                    builder.like(builder.lower(root.get("name")), "%$it%"),
                    builder.like(builder.lower(root.get("uniqueName")), "%$it%"),
                    builder.like(builder.lower(root.get("email")), "%$it%"),
                )
            } ?: builder.conjunction()
        }
        val specification = searchSpecification.and(legacyFilters.fromRequest(request, USER_FIELDS))
        return repository.findAll(
            specification,
            PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).content
    }

    @GetMapping("/users/{id}")
    @RequiresPermission("admin.users:read")
    fun getUser(@PathVariable id: String, @CurrentAuthKey authKey: AuthKey): User =
        repository.findById(id).orElseThrow(::NotFoundException)

    @DeleteMapping("/users/{id}")
    @RequiresPermission("admin.users:delete")
    fun removeUser(@PathVariable id: String, @CurrentAuthKey authKey: AuthKey): ActionResponse {
        users.delete(repository.findById(id).orElseThrow(::NotFoundException))
        return ActionResponse(success = true)
    }

    @PutMapping("/users/{id}")
    @RequiresPermission("admin.users:edit")
    fun editUser(
        @PathVariable id: String,
        @Valid @RequestBody request: EditUserRequest,
        @CurrentAuthKey authKey: AuthKey
    ): ActionResponse {
        val user = repository.findById(id).orElseThrow(::NotFoundException)
        request.name?.let { user.name = it }
        request.uniqueName?.let { user.uniqueName = it }
        request.type?.let { user.type = it }
        repository.save(user)
        return ActionResponse(success = true)
    }
}
