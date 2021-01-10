package com.ngenenius.api.reactive.service

import com.ngenenius.api.config.TwitchProperties
import com.ngenenius.api.config.TwitchStreamsProperties
import com.ngenenius.api.model.twitch.OAuthTokenResponse
import com.ngenenius.api.model.twitch.StreamDetailsResponse
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

private val global = KotlinLogging.logger { }

private val NO_CACHE: () -> Duration = { Duration.ZERO }
private val NO_CACHE_ERROR: (Throwable) -> Duration = { global.error("Failed request, will not cache!", it); Duration.ZERO }

@Deprecated("Will be removed")
@Service
class TwitchService(private val webClient: WebClient, private val twitch: TwitchProperties) {

    private val authenticationPublisher = TwitchServiceAuthentication.authenticationPublisher(webClient, twitch)
    private val teamViewStreamDetails =
        TwitchStreamDetails.streamDetailsPublisher(webClient, twitch, authenticationPublisher) { teamView }
    private val tournamentStreamDetails =
        TwitchStreamDetails.streamDetailsPublisher(webClient, twitch, authenticationPublisher) { teamView }

    fun teamViewStreamDetails(): Mono<StreamDetailsResponse> {
        return teamViewStreamDetails
    }

    fun tournamentViewStreamDetails(): Mono<StreamDetailsResponse> {
        return tournamentStreamDetails
    }

}

@Deprecated("Will be removed")
private object TwitchServiceAuthentication {

    val logger = KotlinLogging.logger { }

    /**
     * If we access this publisher from a static context and re-use the initial value
     * of this method, we effectively have our authentication token cached for the length of the
     * life of the token (minus 5 seconds), and we'll retrieve a new token only when our token is about to expire, or has already expired.
     *
     * SLICK A.F.
     */
    fun authenticationPublisher(webClient: WebClient, twitch: TwitchProperties): Mono<OAuthTokenResponse> {
        return webClient
                .post()
                .uri("https://id.twitch.tv/oauth2/token?client_id=${twitch.auth.clientId}&client_secret=${twitch.auth.clientSecret}&grant_type=client_credentials")
                .exchange()
                .flatMap { it.bodyToMono<OAuthTokenResponse>() }
                .doOnEach {
                    it.get()?.apply{
                        logger.info("Received an authentication token! type [{}], valid for [{} minutes]",
                                this.tokenType,
                                Duration.ofMillis(this.expiresIn).toMinutes()
                        )
                    }
                }
                .cache({ Duration.ofMillis(it.expiresIn - 5000) }, NO_CACHE_ERROR, NO_CACHE)
    }
}

@Deprecated("Will be removed")
private object TwitchStreamDetails {
    val logger = KotlinLogging.logger { }

    /**
     * Another cacheable publisher that receives a web client, twitch properties, and a mono of our OAuth Token response.
     * This publisher queries the twitch api for stream details and returns the results.
     *
     * In the new Twitch API:
     * curl -H "Authorization: Bearer <access token>" https://api.twitch.tv/helix/
     */
    fun streamDetailsPublisher(webClient: WebClient,
                               twitch: TwitchProperties,
                               authentication: Mono<OAuthTokenResponse>,
                               streamsSelector: TwitchProperties.() -> TwitchStreamsProperties
    ): Mono<StreamDetailsResponse> {
        return authentication.flatMap { auth ->
                    webClient
                            .get()
                            .uri("https://api.twitch.tv/helix/streams?${twitch.streamsSelector().channelsAsQueryParams()}&first=${twitch.streamsSelector().channels.size}")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer ${auth.accessToken}")
                             .header("client-id", twitch.auth.clientId)
                            .exchange()
                }.flatMap { it.bodyToMono<StreamDetailsResponse>() }
                .doOnEach { it.get()?.apply{
                    logger.debug("Received stream details that will be cached for {} seconds!\n[{}]", twitch.streamsSelector().cacheSeconds, this) } }
                .doOnEach { it.get()?.apply{ logger.info("The following streams are currently live: [{}]", this.data.map{ s -> s.userName }) } }
                .cache({ Duration.ofSeconds(twitch.streamsSelector().cacheSeconds) }, NO_CACHE_ERROR, NO_CACHE)
    }
}
