package de.interaapps.pastefy.exceptions

import org.springframework.http.HttpStatus

open class HttpException(
    val status: HttpStatus,
    message: String,
) : RuntimeException(message)

class AuthenticationException(message: String = "Not authenticated") :
    HttpException(HttpStatus.UNAUTHORIZED, message)

class AwaitingAccessException(message: String = "Awaiting access") :
    HttpException(HttpStatus.FORBIDDEN, message)

class BlockedException(message: String = "Blocked") :
    HttpException(HttpStatus.UNAUTHORIZED, message)

class FeatureDisabledException(message: String = "Feature disabled") :
    HttpException(HttpStatus.FORBIDDEN, message)

class NotFoundException(message: String = "Resource not found") :
    HttpException(HttpStatus.NOT_FOUND, message)

class PastePrivateException(message: String = "Paste is private") :
    HttpException(HttpStatus.FORBIDDEN, message)

class PermissionsDeniedException(message: String = "Permission denied") :
    HttpException(HttpStatus.FORBIDDEN, message)

class TooManyRequestsException(message: String = "Too many requests") :
    HttpException(HttpStatus.TOO_MANY_REQUESTS, message)

class OAuth2Exception(message: String = "OAuth2 authentication failed") :
    HttpException(HttpStatus.UNAUTHORIZED, message)
