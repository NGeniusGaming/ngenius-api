package com.ngenenius.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono


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
}
