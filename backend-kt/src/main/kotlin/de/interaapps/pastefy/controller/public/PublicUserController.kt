package de.interaapps.pastefy.controller.public

import de.interaapps.pastefy.auth.annotations.PublicPastesEnabled
import de.interaapps.pastefy.dto.user.PublicUserDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable

@RestController
@RequestMapping("/api/v2/public/user")
@PublicPastesEnabled
class PublicUserController {
    @GetMapping("/{name}")
    fun getUser(@PathVariable name: String): PublicUserDto = TODO()
}
