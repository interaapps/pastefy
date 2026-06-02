package de.interaapps.pastefy.exceptions

import de.interaapps.pastefy.dto.ExceptionResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {
    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(HttpException::class)
    fun handleHttpException(exception: HttpException): ResponseEntity<ExceptionResponse> =
        ResponseEntity.status(exception.status).body(exception.toResponse())

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(exception: MethodArgumentNotValidException): ResponseEntity<ExceptionResponse> {
        val message = exception.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ExceptionResponse(exception::class.simpleName ?: "ValidationException", message))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(exception: Exception): ResponseEntity<ExceptionResponse> {
        logger.error("Unhandled API exception", exception)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ExceptionResponse(exception::class.simpleName ?: "Exception", "Internal server error"))
    }

    private fun HttpException.toResponse() = ExceptionResponse(
        exception = this::class.simpleName ?: "HttpException",
        message = message,
    )
}
