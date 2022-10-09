package com.ngenenius.api.controller

import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.service.twitch.TwitchStreamsService
import org.mockito.kotlin.*
import org.junit.jupiter.api.Test

internal class TwitchBetaControllerTest {

    private val twitchService = mock<TwitchStreamsService>()
    private val controller = TwitchBetaController(twitchService, mock())

    @Test
    fun `Should use the twitch service for team view details when getting team view`() {
        controller.teamView()

        verify(twitchService, times(1)).streamDetails(eq(StreamingTab.TEAM_VIEW))

        verifyNoMoreInteractions(twitchService)
    }

    @Test
    fun `Should use the twitch service for tournament details when getting tournament view`() {
        controller.tournament()

        verify(twitchService, times(1)).streamDetails(eq(StreamingTab.TOURNAMENT))

        verifyNoMoreInteractions(twitchService)
    }
}
