package de.interaapps.pastefy.controller

import com.fasterxml.jackson.databind.ObjectMapper
import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.dto.pastes.MultiPastesElement
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.enums.PasteType
import de.interaapps.pastefy.service.PasteCommandService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartHttpServletRequest

@RestController
class FormDataPasteController(
    private val commands: PasteCommandService,
    private val objectMapper: ObjectMapper,
) {
    @PostMapping(path = ["/", "/api/asciicasts"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @RateLimited
    @LoginRequiredForCreate
    @RejectAwaitingAccess
    @RejectBlocked
    fun createPaste(request: MultipartHttpServletRequest, @CurrentUser user: User?): ResponseEntity<String> {
        val files = request.fileMap.mapValues { (_, file) -> file.inputStream.bufferedReader().use { it.readText() } }
        if (files.isEmpty()) return ResponseEntity.ok("No file given. curl -F 'f=@filename' pastefy.app\n")
        val asciicast = request.requestURI == "/api/asciicasts"
        val paste = if (files.size == 1) {
            val (name, content) = files.entries.first()
            commands.createUploaded(
                title = if (asciicast) "$name.cast" else name,
                content = content,
                type = PasteType.PASTE,
                user = user,
                tags = if (asciicast) listOf("asciicast", "asciinema") else emptyList(),
            )
        } else {
            commands.createUploaded(
                title = null,
                content = objectMapper.writeValueAsString(files.map { MultiPastesElement(it.key, it.value) }),
                type = PasteType.MULTI_PASTE,
                user = user,
            )
        }
        val host = request.getHeader("Host")
        return if (asciicast) {
            ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(
                objectMapper.writeValueAsString(
                    mapOf(
                        "url" to "$host/${paste.key}",
                        "message" to "Created paste on https://$host/${paste.key}"
                    )
                ),
            )
        } else {
            ResponseEntity.ok("https://$host/${paste.key}\n")
        }
    }
}
