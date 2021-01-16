package com.ngenenius.api.service.twitch

import com.github.benmanes.caffeine.cache.Cache
import com.ngenenius.api.config.TwitchIdentifier
import com.ngenenius.api.config.TwitchStreamerProvider
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.TwitchResponse
import com.ngenenius.api.model.twitch.UserDetails
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class TwitchUsersService(
    twitchWebClient: WebClient,
    twitchStreamerProvider: TwitchStreamerProvider,
    twitchUsersCache: Cache<TwitchIdentifier, UserDetails>,
) : AbstractTwitchPaginatedService<UserDetails>(
    twitchWebClient,
    twitchStreamerProvider,
    twitchUsersCache,
    "/helix/users"
), CacheUsage by PreferCache {

    override val identifierTransformer: (UserDetails) -> TwitchIdentifier = { TwitchIdentifier(it.id, it.displayName) }

    override val valueTypeReference: ParameterizedTypeReference<TwitchResponse<UserDetails>> =
        object : ParameterizedTypeReference<TwitchResponse<UserDetails>>() {}

    fun users(tab: StreamingTab): Collection<UserDetails> = findByTab(tab)

    fun findUsersByLogin(vararg login: String): Collection<UserDetails> =
        findByKeys(login.map { TwitchIdentifier(displayName = it) })

}
