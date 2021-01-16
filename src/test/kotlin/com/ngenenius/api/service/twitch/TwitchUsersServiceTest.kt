package com.ngenenius.api.service.twitch

import com.github.benmanes.caffeine.cache.Caffeine
import com.ngenenius.api.config.TwitchIdentifier
import com.ngenenius.api.config.TwitchStreamerProvider
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.UserDetails
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

private const val response = "{\n" +
        "  \"data\": [{\n" +
        "    \"id\": \"44322889\",\n" +
        "    \"login\": \"dallas\",\n" +
        "    \"display_name\": \"dallas\",\n" +
        "    \"type\": \"staff\",\n" +
        "    \"broadcaster_type\": \"partner\",\n" +
        "    \"description\": \"Just a gamer playing games and chatting. :)\",\n" +
        "    \"profile_image_url\": \"https://static-cdn.jtvnw.net/jtv_user_pictures/dallas-profile_image-1a2c906ee2c35f12-300x300.png\",\n" +
        "    \"offline_image_url\": \"https://static-cdn.jtvnw.net/jtv_user_pictures/dallas-channel_offline_image-1a2c906ee2c35f12-1920x1080.png\",\n" +
        "    \"view_count\": 191836881,\n" +
        "    \"email\": \"login@provider.com\"\n" +
        "  }]\n" +
        "}"

internal class TwitchUsersServiceTest: AbstractTwitchServiceTest<UserDetails>() {

    override val cache = Caffeine.newBuilder().build<TwitchIdentifier, UserDetails>()

    private val twitchStreamerProvider = mock<TwitchStreamerProvider>()

    private lateinit var twitchUsersService: TwitchUsersService

    @BeforeEach()
    fun setup() {
        twitchUsersService = TwitchUsersService(webClient, twitchStreamerProvider, cache)

        whenever(twitchStreamerProvider.twitchIdentifiers(any())).thenReturn(listOf(TwitchIdentifier("44322889", "dallas")))
    }

    @Test
    fun `Should lookup users by tab`() {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody(response)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        val result = twitchUsersService.users(StreamingTab.TEAM_VIEW)

        assertThat(twitchApiMockServer.requestCount).isEqualTo(1)

        assertThat(result.first().displayName).isEqualTo("dallas")
    }

    @Test
    fun `Should lookup users by login`() {
        twitchApiMockServer.enqueue(
            MockResponse().setResponseCode(200)
                .setBody(response)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        )

        val result = twitchUsersService.findUsersByLogin("dallas")

        assertThat(twitchApiMockServer.requestCount).isEqualTo(1)

        assertThat(result.first().displayName).isEqualTo("dallas")
    }
}
