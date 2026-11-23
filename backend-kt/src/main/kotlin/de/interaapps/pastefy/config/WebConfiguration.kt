package de.interaapps.pastefy.config

import de.interaapps.pastefy.auth.AuthArgumentResolver
import de.interaapps.pastefy.auth.AuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Duration

@Configuration
class WebConfiguration(
    private val authInterceptor: AuthInterceptor,
    private val properties: PastefyProperties,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers += AuthArgumentResolver()
    }

    override fun addCorsMappings(registry: org.springframework.web.servlet.config.annotation.CorsRegistry) {
        val origins = properties.cors.split(',')
            .map(String::trim)
            .filter(String::isNotBlank)
        if (origins.isEmpty()) return

        registry.addMapping("/**")
            .allowedOriginPatterns(*origins.toTypedArray())
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(false)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/static/assets/")
            .setCacheControl(
                CacheControl.maxAge(Duration.ofDays(7))
                    .cachePublic()
                    .immutable(),
            )

        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
            .setCacheControl(
                CacheControl.maxAge(Duration.ofHours(1))
                    .cachePublic(),
            )
    }
}
