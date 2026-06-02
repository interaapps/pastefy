package de.interaapps.pastefy.controller

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.entities.User
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartHttpServletRequest

@RestController
class FormDataPasteController {
    @PostMapping(path = ["/", "/api/asciicasts"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @RateLimited
    @LoginRequiredForCreate
    @RejectAwaitingAccess
    @RejectBlocked
    fun createPaste(request: MultipartHttpServletRequest, @CurrentUser user: User?): String = TODO()
}
