package de.interaapps.pastefy.auth

import de.interaapps.pastefy.auth.annotations.*
import de.interaapps.pastefy.auth.ratelimit.InMemoryRateLimiter
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.exceptions.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(
    private val properties: PastefyProperties,
    private val rateLimiter: InMemoryRateLimiter,
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) return true
        val auth = request.getAttribute(RequestAuthAttributes.AUTH) as? RequestAuth

        annotation<RateLimited>(handler)?.let { annotation ->
            if (properties.rateLimiter.enabled) {
                val limit = annotation.limit.takeIf { it > 0 } ?: properties.rateLimiter.limit
                val window = annotation.windowMillis.takeIf { it > 0 } ?: properties.rateLimiter.windowMillis
                val key = "${handler.method.toGenericString()}:${request.remoteAddr}"
                if (!rateLimiter.tryAcquire(key, limit, window)) throw TooManyRequestsException()
            }
        }

        if (has<Authenticated>(handler) && auth == null) throw AuthenticationException()
        if (has<AdminRoute>(handler) && auth?.user?.type != User.Type.ADMIN) throw PermissionsDeniedException()
        if (has<RejectBlocked>(handler) && auth?.user?.type == User.Type.BLOCKED) throw BlockedException()
        if (has<RejectAwaitingAccess>(handler) && auth?.user?.type == User.Type.AWAITING_ACCESS) throw AwaitingAccessException()
        if (has<LoginRequiredForRead>(handler) && properties.loginRequiredRead && auth?.user?.roleCheck() != true) {
            throw AuthenticationException()
        }
        if (has<LoginRequiredForCreate>(handler) && properties.loginRequiredCreate && auth?.user?.roleCheck() != true) {
            throw AuthenticationException()
        }
        if (has<PublicPastesEnabled>(handler) && !properties.publicPastesEnabled) throw FeatureDisabledException()
        annotation<RequiresPermission>(handler)?.let { permission ->
            auth?.authKey?.let { authKey ->
                if (permission.anyOf.isNotEmpty()) authKey.checkPermissions(*permission.anyOf)
                permission.allOf.forEach { authKey.checkPermissions(it) }
            }
        }
        return true
    }

    private inline fun <reified T : Annotation> has(handler: HandlerMethod): Boolean = annotation<T>(handler) != null

    private inline fun <reified T : Annotation> annotation(handler: HandlerMethod): T? =
        AnnotatedElementUtils.findMergedAnnotation(handler.method, T::class.java)
            ?: AnnotatedElementUtils.findMergedAnnotation(handler.beanType, T::class.java)
}
