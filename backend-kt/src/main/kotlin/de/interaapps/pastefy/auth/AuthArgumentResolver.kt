package de.interaapps.pastefy.auth

import de.interaapps.pastefy.auth.annotations.CurrentAuthKey
import de.interaapps.pastefy.auth.annotations.CurrentUser
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class AuthArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentUser::class.java) && parameter.parameterType == User::class.java ||
            parameter.hasParameterAnnotation(CurrentAuthKey::class.java) && parameter.parameterType == AuthKey::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java) ?: return null
        val auth = request.getAttribute(RequestAuthAttributes.AUTH) as? RequestAuth ?: return null
        return if (parameter.hasParameterAnnotation(CurrentUser::class.java)) auth.user else auth.authKey
    }
}
