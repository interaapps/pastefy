package de.interaapps.pastefy.service

import de.interaapps.pastefy.dto.pastes.CreatePasteCommentRequest
import de.interaapps.pastefy.exceptions.HttpException
import de.interaapps.pastefy.repositories.PasteCommentRepository
import de.interaapps.pastefy.repositories.UserRepository
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock

class PasteCommentServiceTest {
    private val service = PasteCommentService(
        mock(PasteService::class.java),
        mock(PasteCommentRepository::class.java),
        mock(UserRepository::class.java),
        mock(PasteMetricsService::class.java),
    )

    @Test
    fun `accepts a valid line range`() {
        assertDoesNotThrow {
            service.validate(CreatePasteCommentRequest(content = "Useful note", lineFrom = 3, lineTo = 8))
        }
    }

    @Test
    fun `rejects blank comments`() {
        val exception = assertThrows<HttpException> {
            service.validate(CreatePasteCommentRequest(content = "  "))
        }

        assertEquals("Comment content is required", exception.message)
    }

    @Test
    fun `rejects line to without line from`() {
        val exception = assertThrows<HttpException> {
            service.validate(CreatePasteCommentRequest(content = "note", lineTo = 3))
        }

        assertEquals("line_to requires line_from", exception.message)
    }
}
