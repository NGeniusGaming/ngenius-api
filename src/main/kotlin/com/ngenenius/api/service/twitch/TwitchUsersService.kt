package com.ngenenius.api.service.twitch

import com.github.benmanes.caffeine.cache.Cache
import com.ngenenius.api.config.TwitchStreamerProvider
import com.ngenenius.api.model.twitch.TwitchResponse
import com.ngenenius.api.model.twitch.UserDetails
import org.springframework.web.reactive.function.client.WebClient

class TwitchUsersService(
    private val twitchWebClient: WebClient,
    private val twitchStreamerProvider: TwitchStreamerProvider,
    private val twitchUsersCache: Cache<String, TwitchResponse<UserDetails>>
) {
}
