package com.ngenenius.api.config

import mu.KotlinLogging
import org.springframework.beans.factory.DisposableBean
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

private val logger = KotlinLogging.logger { }

@Component
class ApplicationCleanup(
    private val twitchAuthorizedClientProvider: OAuth2AuthorizedClientProvider,
    private val clientRegistrationRepository: ClientRegistrationRepository,
) : DisposableBean {

    override fun destroy() {

        logger.info { "In shutdown phase of the application. Attempting to revoke all OAuth2 Tokens." }

        val twitchRegistration = clientRegistrationRepository.findByRegistrationId("twitch")
        val context = OAuth2AuthorizationContext
            .withClientRegistration(twitchRegistration)
            .principal(
                AnonymousAuthenticationToken(
                    "ApplicationCleanup",
                    "anonymousUser",
                    mutableListOf(
                        SimpleGrantedAuthority("ROLE_ANONYMOUS")
                    )
                )
            )
            .build()

        val token = twitchAuthorizedClientProvider.authorize(context)

        logger.info { "Attempting to revoke retrieved Twitch OAuth2 token: [${token?.accessToken?.tokenValue}]" }

        WebClient.builder()
            .filters { filters -> filters.addAll(VerboseWebClientLogger.filters) }
            .baseUrl("https://id.twitch.tv")
            .build()
            .post()
            .uri("/oauth2/revoke?client_id=${token?.clientRegistration?.clientId}&token=${token?.accessToken?.tokenValue}")
            .retrieve()
            .bodyToMono<String>()
            .doOnEach { logger.info { "Raw Response Body: ${it.get()} <- successful calls have no body." } }
            .block(Duration.ofMinutes(1L))

        logger.info { "Completed custom application cleanup, resuming with normal scheduled duties." }
    }

    private object VerboseWebClientLogger {

        private val requestLogger = ExchangeFilterFunction.ofRequestProcessor { request ->
            logger.info { "Executing request: ${request.method()}: ${request.url()}" }
            Mono.just(request)
        }

        private val responseLogger = ExchangeFilterFunction.ofResponseProcessor { response ->
            logger.info { "Received response status: ${response.statusCode()}" }
            Mono.just(response)
        }

        val filters = listOf(requestLogger, responseLogger)
    }

}
