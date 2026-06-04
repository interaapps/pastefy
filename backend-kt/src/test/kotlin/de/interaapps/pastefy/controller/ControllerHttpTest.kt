package de.interaapps.pastefy.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import de.interaapps.pastefy.auth.annotations.CurrentAuthKey
import de.interaapps.pastefy.auth.annotations.CurrentUser
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.controller.public.PublicUserController
import de.interaapps.pastefy.controller.pastes.PasteRawController
import de.interaapps.pastefy.controller.stats.StatsController
import de.interaapps.pastefy.dto.app.StatsResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.infrastructure.ai.PasteAI
import de.interaapps.pastefy.infrastructure.analytics.AnalyticsService
import de.interaapps.pastefy.service.PasteService
import de.interaapps.pastefy.service.PublicPasteEngagementService
import de.interaapps.pastefy.service.StatsService
import de.interaapps.pastefy.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.support.StaticListableBeanFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory

class ControllerHttpTest {
    private val objectMapper = ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    @Test
    fun `raw endpoint returns legacy plain text 404`() {
        val pasteService = mock(PasteService::class.java)
        `when`(pasteService.get("missing")).thenReturn(null)
        val mvc = mockMvc(
            PasteRawController(
                pasteService,
                mock(PublicPasteEngagementService::class.java),
                StaticListableBeanFactory().getBeanProvider(AnalyticsService::class.java),
                objectMapper,
            ),
        )

        mvc.get("/missing/raw")
            .andExpect {
                status { isNotFound() }
                content { contentType("text/plain;charset=UTF-8") }
                content { string("404 - Paste not found") }
            }
    }

    @Test
    fun `stats endpoint serializes snake case response`() {
        val stats = mock(StatsService::class.java)
        `when`(stats.get()).thenReturn(StatsResponse(createdPastes = 12, loggedInPastes = 5))
        val mvc = mockMvc(StatsController(PastefyProperties(publicStats = true), stats))

        mvc.get("/api/v2/app/stats")
            .andExpect {
                status { isOk() }
                jsonPath("$.created_pastes") { value(12) }
                jsonPath("$.logged_in_pastes") { value(5) }
                jsonPath("$.createdPastes") { doesNotExist() }
            }
    }

    @Test
    fun `app info serializes configuration using snake case`() {
        val beans = StaticListableBeanFactory()
        val properties = PastefyProperties(
            customName = "Pastefy",
            encryptionDefault = true,
            publicPastesEnabled = true,
        )
        val mvc = mockMvc(
            AppController(
                properties,
                beans.getBeanProvider(PasteAI::class.java),
                beans.getBeanProvider(AnalyticsService::class.java),
            ),
        )

        mvc.get("/api/v2/app/info")
            .andExpect {
                status { isOk() }
                jsonPath("$.custom_name") { value("Pastefy") }
                jsonPath("$.encryption_is_default") { value(true) }
                jsonPath("$.public_pastes_enabled") { value(true) }
                jsonPath("$.ai_enabled") { value(false) }
            }
    }

    @Test
    fun `public user route returns legacy public shape`() {
        val users = mock(UserService::class.java)
        `when`(users.getByName("julian")).thenReturn(
            User(
                id = "user-01",
                uniqueName = "julian",
                name = "Julian",
            ),
        )
        val mvc = mockMvc(PublicUserController(users))

        mvc.get("/api/v2/public/user/julian")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("julian") }
                jsonPath("$.display_name") { value("Julian") }
            }
    }

    private fun mockMvc(controller: Any): MockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setCustomArgumentResolvers(NullableAuthArgumentResolver())
            .setMessageConverters(StringHttpMessageConverter(), MappingJackson2HttpMessageConverter(objectMapper))
            .build()
}

private class NullableAuthArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentUser::class.java) && parameter.parameterType == User::class.java ||
            parameter.hasParameterAnnotation(CurrentAuthKey::class.java) && parameter.parameterType == AuthKey::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? = null
}
