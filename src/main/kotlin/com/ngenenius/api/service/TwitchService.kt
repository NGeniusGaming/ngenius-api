package com.ngenenius.api.service

import com.github.benmanes.caffeine.cache.Cache
import com.ngenenius.api.config.TwitchProperties
import com.ngenenius.api.config.TwitchStreamsProperties
import com.ngenenius.api.model.twitch.StreamDetailsResponse
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

private val logger = KotlinLogging.logger {  }

@Component
class TwitchService(
    private val twitchWebClient: WebClient,
    private val twitch: TwitchProperties,
    private val twitchResponseCache: Cache<String, StreamDetailsResponse>
) {

    /**
     * Retrieve [StreamDetailsResponse] for the team view page.
     */
    fun teamViewDetails(): StreamDetailsResponse {
        return streamDetails { teamView }
    }

    /**
     * Retrieve [StreamDetailsResponse] for the tournament view page.
     */
    fun tournamentDetails(): StreamDetailsResponse {
        return streamDetails { tournament }
    }

    /**
     * Compute a cache key from the calling function (i.e. [teamViewDetails] or [tournamentDetails])
     * and then pass it off to the cache manager to either return from cache or re-query Twitch for new details.
     */
    private fun streamDetails(streamFn: TwitchProperties.() -> TwitchStreamsProperties): StreamDetailsResponse {
        val stream = twitch.streamFn()
        val key = streamFn.javaClass.simpleName.substringBefore('$')
        if (stream.channels.size > 100) {
            // if we start seeing this log, we need to re-vamp the call to twitch to chunk requests by 100 and merge to a complete response.
            logger.warn("Channels list for $key() is greater than 100. Only the first 100 are returned per call.")
        }
        return internalStreamDetails(key, stream)
    }

    /**
     * Perform a cache lookup from the [key]. If no cached data is found, use the provided [TwitchStreamsProperties]
     * to query (and cache) new data for this [key].  This will prevent abuse of the Twitch API.
     */
    private fun internalStreamDetails(key: String, stream: TwitchStreamsProperties): StreamDetailsResponse {
        logger.debug("Attempting rapid lookup of [{}]", key)
        return twitchResponseCache.get(key) {twitchStreamsRequest(it, stream)} ?: throw NullPointerException("Nothing available from Twitch.")
    }

    /**
     * Execute a GET to the Twitch Helix Streams API to deduce who's online and details about their current stream.
     */
    private fun twitchStreamsRequest(key: String, stream: TwitchStreamsProperties): StreamDetailsResponse {
        logger.debug("cache miss for [{}], retrieving details from Twitch.", key)
        return twitchWebClient.get()
            .uri("/helix/streams?${stream.channelsAsQueryParams()}&first=${stream.channels.size}")
            .header("Client-Id", twitch.auth.clientId)
            .retrieve()
            .onStatus({!it.is2xxSuccessful}, { Mono.just(IllegalStateException("Twitch API Received Status Code: ${it.statusCode()} - Try again later.")) })
            .bodyToMono<StreamDetailsResponse>()
            .block(Duration.ofSeconds(30L)) ?: throw NullPointerException("Received nothing from the Twitch API. Try again later!")
    }

}
