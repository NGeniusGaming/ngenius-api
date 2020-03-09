package com.ngenenius.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ApplicationConfig(private val securityProperties: SecurityProperties) {

    @Bean
    fun webClient(): WebClient {
        return WebClient.create()
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfiguration = CorsConfiguration()

        corsConfiguration.allowedOrigins = securityProperties.cors.allowedDomains
        corsConfiguration.maxAge = 3600
        corsConfiguration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "OPTIONS", "DELETE")

        val urlSource = UrlBasedCorsConfigurationSource()
        urlSource.registerCorsConfiguration("/**", corsConfiguration)

        return CorsWebFilter(urlSource)
    }
}
