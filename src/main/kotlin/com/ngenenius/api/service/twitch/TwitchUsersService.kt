package com.ngenenius.api.service.twitch

import com.github.benmanes.caffeine.cache.Cache
import com.ngenenius.api.config.Channels
import com.ngenenius.api.config.TwitchIdentifier
import com.ngenenius.api.config.TwitchStreamerProvider
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.TwitchResponse
import com.ngenenius.api.model.twitch.UserDetails
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration

private val logger = KotlinLogging.logger { }

@Component
class TwitchUsersService(
    private val twitchWebClient: WebClient,
    private val twitchStreamerProvider: TwitchStreamerProvider,
    private val twitchUsersCache: Cache<TwitchIdentifier, UserDetails>
) {

    fun users(tab: StreamingTab): Collection<UserDetails> {
        val streams = twitchStreamerProvider.twitchIdentifiers(tab)
        return internalUserDetails(streams)
    }

    fun findUsersByLogin(vararg login: String): Collection<UserDetails> {
        return internalUserDetailsByLogin(login.map{ TwitchIdentifier(displayName = it) })
    }

    private fun internalUserDetails(identifiers: Collection<TwitchIdentifier>): Collection<UserDetails> {
        return twitchUsersCache
            .getAll(identifiers) { twitchUsersRequest(it) }
            .values
            .toSet()
    }

    private fun internalUserDetailsByLogin(logins: Iterable<TwitchIdentifier>): Collection<UserDetails> {
        return twitchUsersCache
            .getAll(logins) { twitchUsersRequest(it) }
            .values
            .toSet()
    }

    private fun twitchUsersRequest(mutableChannels: MutableIterable<TwitchIdentifier>): Map<TwitchIdentifier, UserDetails> {
        val channels = mutableChannels.toList()

        val requestUri = "/helix/users?${channels.toQueryParams()}&first=${channels.size}"
        logger.debug { "Request URI is $requestUri" }

        val userDetails = twitchWebClient.get()
            .uri(requestUri)
            .retrieve()
            .onStatus(
                { !it.is2xxSuccessful },
                { Mono.just(IllegalStateException("Twitch API Received Status Code: ${it.statusCode()} - Try again later.")) })
            .bodyToMono<TwitchResponse<UserDetails>>()
            // todo: need to refactor.
            .map { it.data }
            .block(Duration.ofSeconds(30L))
            ?: throw NullPointerException("Received nothing from the Twitch API. Try again later!")
        val mappedResults = userDetails.map { TwitchIdentifier(it.id, it.displayName) to it }.toMap()

        logger.debug { "Received these mapped results\n\n$mappedResults" }
        val missing = channels - mappedResults.keys
        if (missing.isNotEmpty()) {
            logger.error {
                """IMPORTANT PERFORMANCE DEGRADATION ALERT!
                |The following logins were not found:
                |
                |    $missing
                |
                |Please be aware that looking up by login is _case sensitive_ 
                |- Consider looking up by id instead?
                |
                |IMPORTANT: This is a performance problem because these ids will never be cached!"""
                    .trimMargin()
            }
        }
        return mappedResults
    }
}
