package com.ngenenius.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ngenenius.api.model.platform.StreamingTab
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.convert.converter.Converter
import org.springframework.format.FormatterRegistry
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class ApplicationConfig(private val securityProperties: SecurityProperties) {

    /**
     * Create a new default/primary [ObjectMapper] that can deal with Kotlin data classes.
     */
    @Bean
    @Primary
    fun defaultObjectMapper(): ObjectMapper {
        return jacksonObjectMapper()
    }

    /**
     * Standard CORS configuration to restrict allowed domains. CORS is re-checked once-per-hour.
     */
    @Bean
    fun corsConfiguration(): CorsConfiguration {
        val corsConfiguration = CorsConfiguration()

        corsConfiguration.allowedOrigins = securityProperties.cors.allowedDomains
        corsConfiguration.maxAge = 3600
        corsConfiguration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "OPTIONS", "DELETE")

        return corsConfiguration
    }

    /**
     * Enable CORS protection for all endpoints.
     */
    @Bean
    fun corsConfigurationSource(corsConfiguration: CorsConfiguration): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfiguration)
        return source
    }

    /**
     * This API is open, but security must be auto-configured in order to deal with OAuth2 clients.
     */
    @Bean
    fun mvcSecurityAdapter(): WebSecurityConfigurerAdapter {
        return object : WebSecurityConfigurerAdapter() {
            override fun configure(http: HttpSecurity) {
                http.cors()
                    .and()
                    .authorizeRequests()
                    .anyRequest()
                    .permitAll()
            }
        }
    }

    @Bean
    fun webMvcConfigurer(defaultObjectMapper: ObjectMapper): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addFormatters(registry: FormatterRegistry) {
                registry.addConverter(StringToStreamingTabConverter(defaultObjectMapper))
            }
        }
    }
}

/**
 * Delegate to Jackson to convert from a string to a [StreamingTab] when used as a Path parameter.
 */
class StringToStreamingTabConverter(private val objectMapper: ObjectMapper) : Converter<String, StreamingTab> {
    override fun convert(source: String): StreamingTab? = objectMapper.readValue("\"$source\"")
}
