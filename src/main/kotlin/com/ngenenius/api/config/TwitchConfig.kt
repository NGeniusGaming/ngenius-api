package com.ngenenius.api.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.ngenenius.api.model.twitch.StreamDetails
import com.ngenenius.api.model.twitch.UserDetails
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
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

    @Bean
    fun twitchObjectMapper(): ObjectMapper {
        return jacksonObjectMapper().apply {
            this.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
        }
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
    fun twitchWebClient(
        twitchAuthorizedClientManager: OAuth2AuthorizedClientManager,
        twitch: TwitchProperties,
        @Qualifier("twitchObjectMapper")
        twitchObjectMapper: ObjectMapper
    ): WebClient {
        val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(twitchAuthorizedClientManager)
        oauth2Client.setDefaultClientRegistrationId("twitch")

        // use a customized exchange strategy with our customized object mapper.
        val strategies = ExchangeStrategies
            .builder()
            .codecs {
                it.defaultCodecs()
                    .jackson2JsonEncoder(Jackson2JsonEncoder(twitchObjectMapper, MediaType.APPLICATION_JSON))
                it.defaultCodecs()
                    .jackson2JsonDecoder(Jackson2JsonDecoder(twitchObjectMapper, MediaType.APPLICATION_JSON))
            }.build()

        return WebClient.builder()
            .apply(oauth2Client.oauth2Configuration())
            .exchangeStrategies(strategies)
            .defaultHeader("Client-Id", twitch.auth.clientId)
            // the domain of the Twitch API
            .baseUrl("https://api.twitch.tv")
            .build()
    }

    /**
     * A caffeine cache that expires items every 60 seconds,
     * meaning at most we will query twitch once-per-minute, per-dataset, (per instance)
     */
    @Bean
    fun twitchStreamsCache(): Cache<String, StreamDetails> {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(60L))
            .build()
    }

    /**
     * This caffeine cache expires items every 5 minutes for UserDetails,
     * which update less frequently nor demand as real-time of stats as the
     * details of a potential live stream.
     */
    @Bean
    fun twitchUsersCache(): Cache<String, UserDetails> {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5L))
            .build()
    }
}
