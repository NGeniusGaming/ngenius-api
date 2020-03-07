package com.ngenenius.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ApplicationConfig {

    @Bean
    fun webClient(): WebClient {
        return WebClient.create()
    }
}