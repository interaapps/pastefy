package de.interaapps.pastefy.controller.public

import de.interaapps.pastefy.auth.annotations.PublicPastesEnabled
import de.interaapps.pastefy.dto.user.PublicUserDto
import de.interaapps.pastefy.exceptions.NotFoundException
import de.interaapps.pastefy.service.UserService
import de.interaapps.pastefy.service.toPublicDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable

@RestController
@RequestMapping("/api/v2/public/user")
@PublicPastesEnabled
class PublicUserController(
    private val users: UserService,
) {
    @GetMapping("/{name}")
    fun getUser(@PathVariable name: String): PublicUserDto =
        users.getByName(name)?.toPublicDto() ?: throw NotFoundException()
}
