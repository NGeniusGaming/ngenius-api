package com.ngenenius.api.service.twitch

import com.github.benmanes.caffeine.cache.Cache
import com.ngenenius.api.config.Channels
import com.ngenenius.api.config.TwitchIdentifier
import com.ngenenius.api.config.TwitchStreamerProvider
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.StreamDetails
import com.ngenenius.api.model.twitch.TwitchResponse
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

private val logger = KotlinLogging.logger { }

@Component
class TwitchStreamsService(
    private val twitchWebClient: WebClient,
    private val twitchStreamerProvider: TwitchStreamerProvider,
    private val twitchStreamsCache: Cache<TwitchIdentifier, StreamDetails>
) {

    /**
     * Retrieve [TwitchResponse<StreamDetails>] for the team view page.
     */
    fun teamViewDetails(): Collection<StreamDetails> {
        return streamDetails { twitchIdentifiers(StreamingTab.TEAM_VIEW) }
    }

    /**
     * Retrieve [TwitchResponse<StreamDetails>] for the tournament view page.
     */
    fun tournamentDetails(): Collection<StreamDetails> {
        return streamDetails { twitchIdentifiers(StreamingTab.TOURNAMENT) }
    }

    /**
     * Compute a cache key from the calling function (i.e. [teamViewDetails] or [tournamentDetails])
     * and then pass it off to the cache manager to either return from cache or re-query Twitch for new details.
     */
    private fun streamDetails(streamFn: TwitchStreamerProvider.() -> Collection<TwitchIdentifier>): Collection<StreamDetails> {
        val identifiers = twitchStreamerProvider.streamFn()
        if (identifiers.size > 100) {
            // if we start seeing this log, we need to re-vamp the call to twitch to chunk requests by 100 and merge to a complete response.
            logger.warn(
                "Channels list of $identifiers is greater than 100. Only the first 100 are returned per call. The following channels will be dropped: ${
                    identifiers.drop(
                        100
                    )
                }"
            )
        }
        return internalStreamDetails(identifiers)
    }

    /**
     * Perform a cache lookup from the streamer names in configuration.  If a streamer is missing,
     * request the details for that streamer from the Twitch API and add it to the cache.
     */
    private fun internalStreamDetails(identifiers: Collection<TwitchIdentifier>): Collection<StreamDetails> {
        return twitchStreamsCache
            .getAll(identifiers) { twitchStreamsRequest(it) }
            .values
            .toSet()
    }

    /**
     * Execute a GET to the Twitch Helix Streams API to deduce who's online and details about their current stream.
     */
    // todo: query params are prefixed with 'user_'
    private fun twitchStreamsRequest(mutableIdentifiers: MutableIterable<TwitchIdentifier>): Map<TwitchIdentifier, StreamDetails> {
        val channels = mutableIdentifiers.toList()
        val streamDetails = twitchWebClient.get()
            .uri("/helix/streams?${channels.toQueryParams("user_")}&first=${channels.size}")
            .retrieve()
            .onStatus(
                { !it.is2xxSuccessful },
                { Mono.just(IllegalStateException("Twitch API Received Status Code: ${it.statusCode()} - Try again later.")) })
            .bodyToMono<TwitchResponse<StreamDetails>>()
            // todo: need to solve pagination?
            .map { it.data }
            .block(Duration.ofSeconds(30L))
            ?: throw NullPointerException("Received nothing from the Twitch API. Try again later!")

        return streamDetails.map { TwitchIdentifier(it.userId, it.userName) to it }.toMap()
    }

}
