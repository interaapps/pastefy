package de.interaapps.pastefy.controller

import de.interaapps.pastefy.service.FrontendIndexService
import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@Order(Ordered.LOWEST_PRECEDENCE)
class FrontendController(
    private val frontendIndex: FrontendIndexService,
) : ErrorController {
    @GetMapping("/")
    fun index(): ResponseEntity<String> = frontend()

    @RequestMapping("/error")
    fun error(request: HttpServletRequest): ResponseEntity<String> {
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as? Int
        val path = (request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) as? String).orEmpty().trimStart('/')

        if (status != HttpStatus.NOT_FOUND.value() || path.isBackendPath()) {
            return ResponseEntity.notFound().build()
        }

        return frontend()
    }

    private fun frontend(): ResponseEntity<String> {
        val html = frontendIndex.html ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok()
            .contentType(MediaType("text", "html", Charsets.UTF_8))
            .body(html)
    }

    private fun String.isBackendPath(): Boolean =
        this == "api" || startsWith("api/")
}
