package com.ngenenius.api.service.twitch

import com.github.benmanes.caffeine.cache.Cache
import com.ngenenius.api.config.TwitchIdentifier
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.web.reactive.function.client.WebClient

/**
 * A common base class to deal with the complications of the mock web server and cache.
 */
abstract class AbstractTwitchServiceTest<VALUE> () {

    protected lateinit var twitchApiMockServer: MockWebServer
    protected lateinit var webClient: WebClient

    protected abstract val cache: Cache<TwitchIdentifier, VALUE>

    @BeforeEach
    fun baseSetup() {
        // clear the cache
        cache.invalidateAll()

        // start a mock http server
        twitchApiMockServer = MockWebServer()
        val url = twitchApiMockServer.url("/").toString()
        // create beans / class under test
        webClient = WebClient.create(url)
    }

    @AfterEach
    fun baseTeardown() {
        // clear the cache
        cache.invalidateAll()

        // stop the server
        twitchApiMockServer.shutdown()
    }
}
