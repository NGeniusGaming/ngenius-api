package com.ngenenius.api.service.twitch

import com.github.benmanes.caffeine.cache.Cache
import com.ngenenius.api.config.TwitchIdentifier
import com.ngenenius.api.config.TwitchStreamerProvider
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.StreamDetails
import com.ngenenius.api.model.twitch.TwitchResponse
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class TwitchStreamsService(
    twitchWebClient: WebClient,
    twitchStreamerProvider: TwitchStreamerProvider,
    twitchStreamsCache: Cache<TwitchIdentifier, StreamDetails>
): AbstractTwitchPaginatedService<StreamDetails>(
    twitchWebClient,
    twitchStreamerProvider,
    twitchStreamsCache,
    "/helix/streams",
    "user_"
), CacheUsage by PreferRealtime {

    override val identifierTransformer: (StreamDetails) -> TwitchIdentifier = { TwitchIdentifier(it.userId, it.userName) }

    override val valueTypeReference: ParameterizedTypeReference<TwitchResponse<StreamDetails>> = object: ParameterizedTypeReference<TwitchResponse<StreamDetails>>(){}

    /**
     * Retrieve [TwitchResponse<StreamDetails>] for the team view page.
     */
    fun teamViewDetails(): Collection<StreamDetails> = findByTab(StreamingTab.TEAM_VIEW)

    /**
     * Retrieve [TwitchResponse<StreamDetails>] for the tournament view page.
     */
    fun tournamentDetails(): Collection<StreamDetails> = findByTab(StreamingTab.TOURNAMENT)

}
