package de.interaapps.pastefy.controller.pastes

import de.interaapps.pastefy.dto.ActionResponse
import de.interaapps.pastefy.dto.pastes.CreatePasteRequest
import de.interaapps.pastefy.dto.pastes.CreatePasteResponse
import de.interaapps.pastefy.dto.pastes.EditPasteRequest
import de.interaapps.pastefy.dto.pastes.PasteResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/paste")
class PasteController {
    @PostMapping
    fun createPaste(
        @Valid @RequestBody request: CreatePasteRequest
    ): PasteResponse {
        TODO()
    }

    @GetMapping
    fun getPastes(@PathVariable id: String): List<PasteResponse> {
        TODO()
    }

    @GetMapping("/{id}")
    fun getPaste(
        @PathVariable id: String,
        @RequestParam fromFrontend: Boolean = false
    ): PasteResponse {
        TODO()
    }

    @PutMapping("/{id}")
    fun putPaste(
        @PathVariable id: String,
        @Valid @RequestBody request: EditPasteRequest,
    ): CreatePasteResponse {
        TODO()
    }

    @DeleteMapping("/{id}")
    fun deletePaste(@PathVariable id: String): ActionResponse {
        TODO()
    }


    @PutMapping("/{id}/star")
    fun addStarToPaste(@PathVariable id: String): ActionResponse {
        TODO()
    }

    @DeleteMapping("/{id}/star")
    fun removeStarFromPaste(@PathVariable id: String): ActionResponse {
        TODO()
    }
}