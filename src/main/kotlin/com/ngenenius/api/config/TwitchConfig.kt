package com.ngenenius.api.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.ngenenius.api.model.twitch.StreamDetails
import com.ngenenius.api.model.twitch.TwitchResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.web.reactive.function.client.WebClient

import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import java.time.Duration

@Configuration
class TwitchConfig {

    /**
     * The twitchAuthorizedClientManager created by deals with client credentials + refresh tokens
     * for interacting with the Twitch API.
     */
    @Bean
    fun twitchAuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientRepository: OAuth2AuthorizedClientRepository
    ): OAuth2AuthorizedClientManager {
        // declare which pieces of the OAuth2 Spec this provider should enable
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .refreshToken()
            .build()
        // create a client manager with this authorized client repository
        val twitchAuthorizedClientManager = DefaultOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientRepository
        )
        twitchAuthorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return twitchAuthorizedClientManager
    }

    /**
     * Create a web client for the Twitch API
     *
     * (configured via properties groups
     *     spring.security.oauth2.client.registration.twitch.*
     *     spring.security.oauth2.client.provider.twitch.* )
     *
     * Using the secrets and configuration properties for the twitch OAuth2 API,
     * an outbound OAuth2 filter function is added to this [WebClient] that will
     *
     *     a) Request an OAuth2 Token if it _doesn't_ have one.
     *     b) Re-use a valid OAuth2 Token if it _does_ have one.
     *     c) Refresh the OAuth2 Token if the refresh token is not expired but token is.
     *     d) Rinse and repeat to serendipity. Yay Spring!
     *
     * The OAuth2 Token from this filter is added as the 'Authorization' header for all requests
     * made by this web client.
     */
    @Bean
    fun twitchWebClient(twitchAuthorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
        val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(twitchAuthorizedClientManager)
        oauth2Client.setDefaultClientRegistrationId("twitch")
        return WebClient.builder()
            .apply(oauth2Client.oauth2Configuration())
            // the domain of the Twitch API
            .baseUrl("https://api.twitch.tv")
            .build()
    }

    /**
     * A very small caffeine cache that expires items every 60 seconds,
     * meaning at most we will query twitch once-per-minute, per-dataset, (per instance)
     */
    @Bean
    fun twitchStreamsCache(): Cache<String, TwitchResponse<StreamDetails>> {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(60L))
            .build()
    }
}
