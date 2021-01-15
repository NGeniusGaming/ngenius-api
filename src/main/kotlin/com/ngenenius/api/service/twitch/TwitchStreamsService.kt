package com.ngenenius.api.service.twitch

import com.github.benmanes.caffeine.cache.Cache
import com.ngenenius.api.config.*
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.StreamDetails
import com.ngenenius.api.model.twitch.TwitchResponse
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

private val logger = KotlinLogging.logger {  }

@Component
class TwitchStreamsService(
    private val twitchWebClient: WebClient,
    private val twitch: TwitchProperties,
    private val twitchStreamerProvider: TwitchStreamerProvider,
    private val twitchStreamsCache: Cache<String, TwitchResponse<StreamDetails>>
) {

    /**
     * Retrieve [TwitchResponse<StreamDetails>] for the team view page.
     */
    fun teamViewDetails(): TwitchResponse<StreamDetails> {
        return streamDetails { twitchStreamersFor(StreamingTab.TEAM_VIEW) }
    }

    /**
     * Retrieve [TwitchResponse<StreamDetails>] for the tournament view page.
     */
    fun tournamentDetails(): TwitchResponse<StreamDetails> {
        return streamDetails { twitchStreamersFor(StreamingTab.TOURNAMENT) }
    }

    /**
     * Compute a cache key from the calling function (i.e. [teamViewDetails] or [tournamentDetails])
     * and then pass it off to the cache manager to either return from cache or re-query Twitch for new details.
     */
    private fun streamDetails(streamFn: TwitchStreamerProvider.() -> Channels): TwitchResponse<StreamDetails> {
        val stream = twitchStreamerProvider.streamFn()
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
    private fun internalStreamDetails(key: String, stream: Channels): TwitchResponse<StreamDetails> {
        logger.debug("Attempting rapid lookup of [{}]", key)
        return twitchStreamsCache.get(key) {twitchStreamsRequest(it, stream)} ?: throw NullPointerException("Nothing available from Twitch.")
    }

    /**
     * Execute a GET to the Twitch Helix Streams API to deduce who's online and details about their current stream.
     */
    private fun twitchStreamsRequest(key: String, stream: Channels): TwitchResponse<StreamDetails> {
        logger.debug("cache miss for [{}], retrieving details from Twitch.", key)
        return twitchWebClient.get()
            .uri("/helix/streams?${stream.channelsAsQueryParams()}&first=${stream.channels.size}")
            .header("Client-Id", twitch.auth.clientId)
            .retrieve()
            .onStatus({!it.is2xxSuccessful}, { Mono.just(IllegalStateException("Twitch API Received Status Code: ${it.statusCode()} - Try again later.")) })
            .bodyToMono<TwitchResponse<StreamDetails>>()
            .block(Duration.ofSeconds(30L)) ?: throw NullPointerException("Received nothing from the Twitch API. Try again later!")
    }

}
