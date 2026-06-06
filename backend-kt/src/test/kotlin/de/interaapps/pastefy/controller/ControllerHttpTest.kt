package de.interaapps.pastefy.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import de.interaapps.pastefy.auth.annotations.CurrentAuthKey
import de.interaapps.pastefy.auth.annotations.CurrentUser
import de.interaapps.pastefy.auth.oauth.OAuth2ProviderRegistry
import de.interaapps.pastefy.config.PastefyProperties
import de.interaapps.pastefy.controller.public.PublicUserController
import de.interaapps.pastefy.controller.seo.PasteMetaSSRController
import de.interaapps.pastefy.controller.pastes.PasteRawController
import de.interaapps.pastefy.controller.stats.StatsController
import de.interaapps.pastefy.controller.user.UserController
import de.interaapps.pastefy.dto.app.StatsResponse
import de.interaapps.pastefy.entities.AuthKey
import de.interaapps.pastefy.entities.User
import de.interaapps.pastefy.infrastructure.ai.PasteAI
import de.interaapps.pastefy.infrastructure.analytics.AnalyticsService
import de.interaapps.pastefy.repositories.PasteAIInfoRepository
import de.interaapps.pastefy.repositories.PasteRepository
import de.interaapps.pastefy.repositories.SharedPasteRepository
import de.interaapps.pastefy.service.FolderService
import de.interaapps.pastefy.service.FrontendIndexService
import de.interaapps.pastefy.service.PasteService
import de.interaapps.pastefy.service.PasteQueryService
import de.interaapps.pastefy.service.PublicPasteEngagementService
import de.interaapps.pastefy.service.SeoPageCacheService
import de.interaapps.pastefy.service.SeoPageContentService
import de.interaapps.pastefy.service.SeoRenderer
import de.interaapps.pastefy.service.StatsService
import de.interaapps.pastefy.service.UserService
import jakarta.servlet.RequestDispatcher
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

    @Test
    fun `logged out user response still exposes configured auth types`() {
        val providers = mock(OAuth2ProviderRegistry::class.java)
        `when`(providers.names()).thenReturn(linkedSetOf("interaapps", "github"))
        val mvc = mockMvc(
            UserController(
                mock(UserService::class.java),
                mock(FolderService::class.java),
                mock(PasteQueryService::class.java),
                mock(PasteRepository::class.java),
                mock(SharedPasteRepository::class.java),
                providers,
            ),
        )

        mvc.get("/api/v2/user")
            .andExpect {
                status { isOk() }
                jsonPath("$.logged_in") { value(false) }
                jsonPath("$.auth_types[0]") { value("interaapps") }
                jsonPath("$.auth_types[1]") { value("github") }
            }
    }

    @Test
    fun `frontend root serves prepared index`() {
        val properties = PastefyProperties(customHeader = "<meta name=\"x-test\" content=\"ok\">")
        val mvc = mockMvc(FrontendController(FrontendIndexService(properties)))

        mvc.get("/")
            .andExpect {
                status { isOk() }
                content { contentType("text/html;charset=UTF-8") }
                content { string(org.hamcrest.Matchers.containsString("<div id=\"app\"></div>")) }
                content { string(org.hamcrest.Matchers.containsString("<meta name=\"x-test\" content=\"ok\">")) }
            }
    }

    @Test
    fun `frontend error fallback serves prepared index for spa routes`() {
        val mvc = mockMvc(FrontendController(FrontendIndexService(PastefyProperties())))

        mvc.get("/error") {
            requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
            requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/fewnadscfknjajsf/whrsdefncwas")
        }.andExpect {
            status { isOk() }
            content { contentType("text/html;charset=UTF-8") }
            content { string(org.hamcrest.Matchers.containsString("<div id=\"app\"></div>")) }
        }
    }

    @Test
    fun `frontend fallback does not swallow api routes`() {
        val mvc = mockMvc(FrontendController(FrontendIndexService(PastefyProperties())))

        mvc.get("/error") {
            requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
            requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/api/does-not-exist")
        }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `frontend error fallback also serves prepared index for missing assets`() {
        val mvc = mockMvc(FrontendController(FrontendIndexService(PastefyProperties())))

        mvc.get("/error") {
            requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
            requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/assets/missing.js")
        }.andExpect {
            status { isOk() }
            content { contentType("text/html;charset=UTF-8") }
        }
    }

    @Test
    fun `missing paste seo route falls back to frontend index`() {
        val pasteService = mock(PasteService::class.java)
        `when`(pasteService.get("missing1")).thenReturn(null)
        val properties = PastefyProperties()
        val frontend = FrontendIndexService(properties)
        val mvc = mockMvc(
            PasteMetaSSRController(
                pasteService,
                mock(PasteAIInfoRepository::class.java),
                properties,
                SeoRenderer(properties, frontend),
                mock(SeoPageCacheService::class.java),
                mock(SeoPageContentService::class.java),
                frontend,
            ),
        )

        mvc.get("/missing1")
            .andExpect {
                status { isOk() }
                content { contentType("text/html;charset=UTF-8") }
                content { string(org.hamcrest.Matchers.containsString("<div id=\"app\"></div>")) }
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
