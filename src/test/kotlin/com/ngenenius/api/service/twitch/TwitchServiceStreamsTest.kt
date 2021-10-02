package com.ngenenius.api.service.twitch

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.benmanes.caffeine.cache.Caffeine
import com.ngenenius.api.config.TwitchIdentifier
import com.ngenenius.api.config.TwitchStreamerProvider
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.StreamDetails
import com.ngenenius.api.model.twitch.TwitchResponse
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.concurrent.TimeUnit

/**
 * A sample, valid streams response containing 2 stream details.
 */
private const val validResponse =
    """{"data":[{"id":"1","user_id":"1","user_name":"channel1","user_login":"channel1","game_id":"1","game_name": "Destiny","type":"live","is_mature":true,"title":"FirstAwesomeStream","viewer_count":21,"started_at":"2021-01-10T06:29:08Z","language":"en","thumbnail_url":"https://some.picture1.jpg","tag_ids":["6ea6bca4-4712-4ab9-a906-e3336a9d8039"]},{"id":"2","user_id":"2","user_name":"channel2","user_login":"channel2","game_id":"2","game_name": "Halo","type":"live","is_mature":false,"title":"SecondAwesomeStream","viewer_count":3,"started_at":"2021-01-10T04:34:30Z","language":"en","thumbnail_url":"https://some.picture2.jpg","tag_ids":["6ea6bca4-4712-4ab9-a906-e3336a9d8039"]}],"pagination":{"cursor":""}}"""

// TODO: this test also tests caching, need to abstract that out to it's own test.
internal class TwitchStreamsServiceTest : AbstractTwitchServiceTest<StreamDetails>() {

    private val twitchStreamerProvider = mock<TwitchStreamerProvider>()

    // keys must match data in the valid response object usernames.
    private val teamView =
        listOf(TwitchIdentifier(displayName = "channel1"), TwitchIdentifier(displayName = "channel2"))
    private val tournament =
        listOf(TwitchIdentifier(displayName = "channel1"), TwitchIdentifier(displayName = "channel2"))

    override val cache = Caffeine.newBuilder().build<TwitchIdentifier, StreamDetails>()

    private lateinit var streamsService: TwitchStreamsService

    @BeforeEach
    fun setup() {
        streamsService = TwitchStreamsService(webClient, twitchStreamerProvider, cache)

        whenever(twitchStreamerProvider.twitchIdentifiers(eq(StreamingTab.TEAM_VIEW))).thenReturn(teamView)
        whenever(twitchStreamerProvider.twitchIdentifiers(eq(StreamingTab.TOURNAMENT))).thenReturn(tournament)
    }

    @MethodSource("streamingTab")
    @ParameterizedTest
    fun `Should query twitch for team view stream details`(tab: StreamingTab) {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody(validResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        val result = streamsService.streamDetails(tab)

        val objectMapper = jacksonObjectMapper()
        // the api extracts the data and reduces to a set.
        val expectedResult = objectMapper.readValue<TwitchResponse<StreamDetails>>(validResponse).data.toSet()

        assertThat(result).isEqualTo(expectedResult)
    }

    @MethodSource("streamingTab")
    @ParameterizedTest
    fun `Subsequent calls to this service should pull from the cache`(tab: StreamingTab) {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody(validResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        // call multiple times quick.
        (0..10).map { streamsService.streamDetails(tab) }

        assertThat(twitchApiMockServer.requestCount).isEqualTo(1)
    }

    @MethodSource("streamingTab")
    @ParameterizedTest
    fun `Should hit the helix streams endpoint`(tab: StreamingTab) {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody(validResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        streamsService.streamDetails(tab)

        val request = twitchApiMockServer.takeRequest(5L, TimeUnit.SECONDS)

        assertThat(request.path)
            .containsPattern("^/helix/streams\\?(user_login=[^\\d]+\\d&?){2}&first=2\$".toRegex().toPattern())

    }

    @MethodSource("streamingTab")
    @ParameterizedTest
    fun `Successfully getting stream details caches the response`(tab: StreamingTab) {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody(validResponse)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        streamsService.streamDetails(tab)

        assertThat(cache.asMap()).hasSize(4)
            .containsKeys(
                TwitchIdentifier(displayName = "channel1"),
                TwitchIdentifier(displayName = "channel2"),
                TwitchIdentifier(id = "1"),
                TwitchIdentifier(id = "2")
            )
    }

    @MethodSource("streamingTab")
    @ParameterizedTest
    fun `A non-successful response status code should throw an exception`(tab: StreamingTab) {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(404)
        )

        assertThatThrownBy { streamsService.streamDetails(tab) }
            .isExactlyInstanceOf(IllegalStateException::class.java)
            .hasMessage("Twitch API Received Status Code: 404 NOT_FOUND - Try again later.")

        assertThat(cache.asMap()).isEmpty()
    }

    companion object {

        @JvmStatic
        @Suppress("unused")
        fun streamingTab() = listOf(
            StreamingTab.TEAM_VIEW,
            StreamingTab.TOURNAMENT
        )
    }

    internal class MethodUnderTest(
        private val name: String,
        val fn: TwitchStreamsService.() -> Collection<StreamDetails>
    ) {
        override fun toString() = name
    }

}
