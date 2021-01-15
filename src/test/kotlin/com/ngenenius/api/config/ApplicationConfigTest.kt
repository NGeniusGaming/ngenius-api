package com.ngenenius.api.config

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.util.UrlPathHelper.PATH_ATTRIBUTE
import javax.servlet.http.HttpServletRequest

internal class ApplicationConfigTest {

    private val securityProperties = mock<SecurityProperties>()

    private val corsProperties = mock<CorsProperties>()
    private val allowedDomains = listOf("http://example.com", "https://second.domain.com")

    private val config = ApplicationConfig(securityProperties)

    @BeforeEach
    fun setup() {
        whenever(securityProperties.cors).thenReturn(corsProperties)
        whenever(corsProperties.allowedDomains).thenReturn(allowedDomains)
    }

    @Test
    fun `Should create cors configuration with configured domains`() {
        val result = config.corsConfiguration()

        assertThat(result.allowedOrigins).isEqualTo(allowedDomains)
        assertThat(result.maxAge).isEqualTo(3600)
        assertThat(result.allowedMethods)
            .containsExactlyInAnyOrder("GET", "POST", "PUT", "PATCH", "OPTIONS", "DELETE")
    }

    @Test
    fun `Should apply cors protection to all URLs`() {
        val corsConfiguration = mock<CorsConfiguration>()

        val result = config.corsConfigurationSource(corsConfiguration)

        val mockRequest = mock<HttpServletRequest>()
        whenever(mockRequest.getAttribute(eq(PATH_ATTRIBUTE))).thenReturn("/")

        assertThat(result.getCorsConfiguration(mockRequest)).isEqualTo(corsConfiguration)
    }

}
