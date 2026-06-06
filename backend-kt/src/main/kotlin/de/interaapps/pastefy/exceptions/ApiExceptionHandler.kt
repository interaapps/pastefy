package de.interaapps.pastefy.exceptions

import de.interaapps.pastefy.dto.ExceptionResponse
import de.interaapps.pastefy.service.FrontendIndexService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class ApiExceptionHandler(
    private val frontendIndex: FrontendIndexService,
) {
    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(HttpException::class)
    fun handleHttpException(exception: HttpException): ResponseEntity<ExceptionResponse> =
        ResponseEntity.status(exception.status).body(exception.toResponse())

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(exception: MethodArgumentNotValidException): ResponseEntity<ExceptionResponse> {
        val message = exception.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest()
            .body(ExceptionResponse(exception::class.simpleName ?: "ValidationException", message))
    }

    @ExceptionHandler(NoResourceFoundException::class, NoHandlerFoundException::class)
    fun handleMissingRoute(exception: Exception, request: HttpServletRequest): ResponseEntity<*> {
        val path = request.requestURI.trimStart('/')
        if (path.isBackendPath()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ExceptionResponse(exception::class.simpleName ?: "NotFoundException", "Not found"))
        }
        return frontendIndex.frontend()
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

    private fun String.isBackendPath(): Boolean =
        this == "api" || startsWith("api/")
}
