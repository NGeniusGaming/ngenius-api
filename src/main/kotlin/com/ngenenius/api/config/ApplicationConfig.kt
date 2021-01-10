package com.ngenenius.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.CorsRegistry
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
    fun reactiveCorsWebFilter(corsConfiguration: CorsConfiguration): CorsWebFilter {
        val urlSource = UrlBasedCorsConfigurationSource()
        urlSource.registerCorsConfiguration("/**", corsConfiguration)

        return CorsWebFilter(urlSource)
    }

    /**
     * Enable CORS protection for all endpoints.
     */
    @Bean
    fun webCorsConfigurer(corsConfiguration: CorsConfiguration): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .combine(corsConfiguration)
            }
        }
    }

    /**
     * This API is open, but security must be auto-configured in order to deal with OAuth2 clients.
     */
    @Bean
    fun mvcSecurityAdapter(): WebSecurityConfigurerAdapter {
        return object : WebSecurityConfigurerAdapter() {
            override fun configure(http: HttpSecurity) {
                http.authorizeRequests()
                    .anyRequest().permitAll()
            }
        }
    }
}
