package com.ngenenius.api.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.benmanes.caffeine.cache.Caffeine
import com.ngenenius.api.config.Channels
import com.ngenenius.api.config.TwitchIdentifier
import com.ngenenius.api.config.TwitchStreamerProvider
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.StreamDetails
import com.ngenenius.api.model.twitch.TwitchResponse
import com.ngenenius.api.service.twitch.TwitchStreamsService
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.util.concurrent.TimeUnit

/**
 * A sample, valid streams response containing 2 stream details.
 */
private const val validResponse =
    "{\"data\":[{\"id\":\"1\",\"user_id\":\"1\",\"user_name\":\"channel1\",\"game_id\":\"1\",\"type\":\"live\",\"title\":\"FirstAwesomeStream\",\"viewer_count\":21,\"started_at\":\"2021-01-10T06:29:08Z\",\"language\":\"en\",\"thumbnail_url\":\"https://some.picture1.jpg\",\"tag_ids\":[\"6ea6bca4-4712-4ab9-a906-e3336a9d8039\"]},{\"id\":\"2\",\"user_id\":\"2\",\"user_name\":\"channel2\",\"game_id\":\"2\",\"type\":\"live\",\"title\":\"SecondAwesomeStream\",\"viewer_count\":3,\"started_at\":\"2021-01-10T04:34:30Z\",\"language\":\"en\",\"thumbnail_url\":\"https://some.picture2.jpg\",\"tag_ids\":[\"6ea6bca4-4712-4ab9-a906-e3336a9d8039\"]}],\"pagination\":{\"cursor\":\"\"}}"

internal class TwitchServiceTest {

    private lateinit var twitchApiMockServer: MockWebServer
    private lateinit var webClient: WebClient

    private val twitchStreamerProvider = mock<TwitchStreamerProvider>()

    // keys must match data in the valid response object usernames.
    private val teamView = listOf(TwitchIdentifier(displayName = "channel1"), TwitchIdentifier(displayName = "channel2"))
    private val tournament = listOf(TwitchIdentifier(displayName = "channel1"), TwitchIdentifier(displayName = "channel2"))

    private val cache = Caffeine.newBuilder().build<TwitchIdentifier, StreamDetails>()

    private lateinit var streamsService: TwitchStreamsService

    @BeforeEach
    fun setup() {
        // clear the cache
        cache.invalidateAll()

        // start a mock http server
        twitchApiMockServer = MockWebServer()
        val url = twitchApiMockServer.url("/").toString()
        // create beans / class under test
        webClient = WebClient.create(url)

        streamsService = TwitchStreamsService(webClient, twitchStreamerProvider, cache)

        whenever(twitchStreamerProvider.twitchIdentifiers(eq(StreamingTab.TEAM_VIEW))).thenReturn(teamView)
        whenever(twitchStreamerProvider.twitchIdentifiers(eq(StreamingTab.TOURNAMENT))).thenReturn(tournament)
    }

    @AfterEach
    fun teardown() {
        // clear the cache
        cache.invalidateAll()

        // stop the server
        twitchApiMockServer.shutdown()
    }

    @MethodSource("publicMethods")
    @ParameterizedTest
    fun `Should query twitch for team view stream details`(methodUnderTest: MethodUnderTest) {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody(validResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        // a cheeky one-liner to extract the function from parameterized object and run it on our service.
        val result = methodUnderTest.fn.let { theMethodUnderTest -> streamsService.theMethodUnderTest() }

        val objectMapper = jacksonObjectMapper()
        // the api extracts the data and reduces to a set.
        val expectedResult = objectMapper.readValue<TwitchResponse<StreamDetails>>(validResponse).data.toSet()

        assertThat(result).isEqualTo(expectedResult)
    }

    @MethodSource("publicMethods")
    @ParameterizedTest
    fun `Subsequent calls to this service should pull from the cache`(methodUnderTest: MethodUnderTest) {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody(validResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        // call multiple times quick.
        (0..10).map { methodUnderTest.fn.let { theMethodUnderTest -> streamsService.theMethodUnderTest() } }

        assertThat(twitchApiMockServer.requestCount).isEqualTo(1)
    }

    @MethodSource("publicMethods")
    @ParameterizedTest
    fun `Should hit the helix streams endpoint`(methodUnderTest: MethodUnderTest) {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody(validResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        methodUnderTest.fn.let { theMethodUnderTest -> streamsService.theMethodUnderTest() }

        val request = twitchApiMockServer.takeRequest(5L, TimeUnit.SECONDS)

        assertThat(request.path)
            .containsPattern("^/helix/streams\\?(user_login=[^\\d]+\\d&?){2}&first=2\$".toRegex().toPattern())

    }

    @MethodSource("publicMethods")
    @ParameterizedTest
    fun `Successfully getting stream details caches the response`(methodUnderTest: MethodUnderTest) {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody(validResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        methodUnderTest.fn.let { theMethodUnderTest -> streamsService.theMethodUnderTest() }

        assertThat(cache.asMap()).hasSize(2).containsKeys(TwitchIdentifier(displayName = "channel1"), TwitchIdentifier(displayName = "channel2"))
    }

    @MethodSource("publicMethods")
    @ParameterizedTest
    fun `A non-successful response status code should throw an exception`(methodUnderTest: MethodUnderTest) {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(404)
        )

        assertThatThrownBy { methodUnderTest.fn.let { theMethodUnderTest -> streamsService.theMethodUnderTest() } }
            .isExactlyInstanceOf(IllegalStateException::class.java)
            .hasMessage("Twitch API Received Status Code: 404 NOT_FOUND - Try again later.")

        assertThat(cache.asMap()).isEmpty()
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun publicMethods() = listOf(
            MethodUnderTest("teamViewDetails()") { teamViewDetails() },
            MethodUnderTest("tournamentDetails()") { tournamentDetails() }
        )
    }

    internal class MethodUnderTest(
        private val name: String,
        val fn: TwitchStreamsService.() -> Collection<StreamDetails>
    ) {
        override fun toString() = name
    }

}
