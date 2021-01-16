package com.ngenenius.api.service.twitch

import com.github.benmanes.caffeine.cache.Cache
import com.ngenenius.api.config.TwitchIdentifier
import com.ngenenius.api.config.TwitchStreamerProvider
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.TwitchResponse
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

private val logger = KotlinLogging.logger {  }

/**
 * This abstract base class will suffice for GET requests to the Twitch API.
 *
 * If the API requires a different request method, this abstract class is probably not for you.
 *
 * The Twitch API has a unique take on pagination, and therefore this should
 * be dealt with in a common approach.
 *
 * To paginate results, twitch can optionally return a 'cursor' reference, which must
 * be sent back to them in the 'after' query parameter in order to receive the next page of results.
 */
abstract class AbstractTwitchPaginatedService<VALUE> (
    /**
     * The twitch web client
     */
    private val twitchWebClient: WebClient,
    /**
     * A mechanism to transform UI tabs to TwitchIdentifiers
     */
    private val twitchStreamerProvider: TwitchStreamerProvider,
    /**
     * The cache for these [VALUE] objects so we don't continually hit the Twitch API.
     */
    private val cache: Cache<TwitchIdentifier, VALUE>,
    /**
     * The piece of the Twitch API call before the query parameter ('?')
     */
    private val rootApiUri: String,
    /**
     * The prefix for all identity related query params for this service. Optional
     */
    private val queryPrefix: String = ""
): CacheUsage {

    /**
     * A reverse-mapping function to take an object retrieved from Twitch and transform it
     * to a [TwitchIdentifier] for caching.
     */
    protected abstract val identifierTransformer: (VALUE) -> TwitchIdentifier

    /**
     * By making extending classes provide a _concrete_ parameterized type reference, the world is happy.
     *
     * A small price to pay for a common abstract class to deal with the complexities of twitch api GET requests.
     */
    internal abstract val valueTypeReference: ParameterizedTypeReference<TwitchResponse<VALUE>>

    protected fun findByTab(tab: StreamingTab): Collection<VALUE> {
        return findByKeys(twitchStreamerProvider.twitchIdentifiers(tab))
    }

    protected fun findByKeys(identifiers: Collection<TwitchIdentifier>): Collection<VALUE> {
        return cache
            .getAll(identifiers) {executeTwitchRequest(it.toMutableSet())}
            .values
            .toSet()
    }

    protected fun findByKeys(vararg identifier: TwitchIdentifier): Collection<VALUE> {
        return findByKeys(identifier.asList())
    }

    private fun executeTwitchRequest(identifiers: MutableCollection<TwitchIdentifier>): Map<TwitchIdentifier, VALUE> {
        if (identifiers.size > 100) {
            val toBeDropped = identifiers - identifiers.drop(100)
            logger.warn { "This API currently only supports 100 identifiers at a time. The following will be dropped: $toBeDropped" }
            identifiers.removeAll(toBeDropped)
        }
        val requestUri = "$rootApiUri?${identifiers.toQueryParams(queryPrefix)}&first=${identifiers.size}"
        logger.trace{ "Request URI is $requestUri"}

        val values = twitchWebClient.get()
            .uri(requestUri)
            .retrieve()
            .onStatus(
                { !it.is2xxSuccessful },
                { Mono.just(IllegalStateException("Twitch API Received Status Code: ${it.statusCode()} - Try again later.")) })
            .bodyToMono(valueTypeReference)
            .map{ it.data }
            .block(Duration.ofSeconds(30L)) ?: throw NullPointerException("Received nothing from the Twitch API. Try again later!")

        val keyed = values.map { identifierTransformer(it) to it }.toMap()

        if (expectPerfectCaching) {
            checkImperfectCacheUsage(keyed.keys, identifiers)
        }

        return keyed
    }

    /**
     * If this operation expects perfect cache usage, this will log a warning message about imperfect cache usage.
     * Not all operations expect perfect cache usage, in an effort to detect things from the Twitch API in closer to real-time.
     */
    private fun checkImperfectCacheUsage(found: Set<TwitchIdentifier>, looking: Collection<TwitchIdentifier>) {
        val missing = looking - found

        if (missing.isNotEmpty()) {
            logger.error {
                """IMPORTANT PERFORMANCE DEGRADATION ALERT!
                |The following identifiers were not found:
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
    }

}

/**
 * Mainly a marker interface to declare the type of cache usage at the service level.
 */
interface CacheUsage {
    val expectPerfectCaching: Boolean
}

/**
 * When the cache is preferred, querying Twitch with things that do not return anything to be cached
 * will be alerted, since this means that we have configured the user incorrectly or that user no longer
 * exists. Either way we should update our configuration.
 */
object PreferCache: CacheUsage {
    override val expectPerfectCaching = true
}

/**
 * When real time data is preferred, or it is expected that this API call will return blanks for keys under
 * different circumstances.  The example of this is the Get Streams API, which only returns users if they are
 * live streaming at the time of the request, thus, we should not alert if we have a cache miss as this is expected.
 */
object PreferRealtime: CacheUsage {
    override val expectPerfectCaching = false
}
