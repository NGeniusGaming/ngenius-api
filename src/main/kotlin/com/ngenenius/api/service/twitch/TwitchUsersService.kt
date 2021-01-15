package com.ngenenius.api.service.twitch

import com.github.benmanes.caffeine.cache.Cache
import com.ngenenius.api.config.Channels
import com.ngenenius.api.config.TwitchStreamerProvider
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.TwitchResponse
import com.ngenenius.api.model.twitch.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class TwitchUsersService(
    private val twitchWebClient: WebClient,
    private val twitchStreamerProvider: TwitchStreamerProvider,
    private val twitchUsersCache: Cache<String, UserDetails>
) {

    fun users(tab: StreamingTab): Collection<UserDetails> {
        val streams = twitchStreamerProvider.twitchStreamersFor(tab)
        return internalUserDetails(streams)
    }

    private fun internalUserDetails(streams: Channels): Collection<UserDetails> {
        return twitchUsersCache
            .getAll(streams.channels) { twitchUsersRequest(streams.channels) }
            .values
            .toSet()
    }

    private fun twitchUsersRequest(channels: List<String>): Map<String, UserDetails> {
        val userDetails = twitchWebClient.get()
            .uri("/helix/users?${channels.toQueryParams("login")}&first=${channels.size}")
            .retrieve()
            .onStatus(
                { !it.is2xxSuccessful },
                { Mono.just(IllegalStateException("Twitch API Received Status Code: ${it.statusCode()} - Try again later.")) })
            .bodyToMono<TwitchResponse<UserDetails>>()
            // todo: need to refactor.
            .map { it.data }
            .block(Duration.ofSeconds(30L))
            ?: throw NullPointerException("Received nothing from the Twitch API. Try again later!")
        return userDetails.map { it.displayName to it }.toMap()
    }
}
