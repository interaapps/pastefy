package de.interaapps.pastefy.controller.public

import de.interaapps.pastefy.dto.user.PublicUserDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/public/user")
class PublicUserController {
    @GetMapping("/{name}")
    fun getUser(): PublicUserDto {
        TODO()
    }
}