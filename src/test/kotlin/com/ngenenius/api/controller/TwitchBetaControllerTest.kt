package com.ngenenius.api.controller

import com.ngenenius.api.service.TwitchService
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.jupiter.api.Test

internal class TwitchBetaControllerTest {

    private val twitchService = mock<TwitchService>()
    private val controller = TwitchBetaController(twitchService)

    @Test
    fun `Should use the twitch service for team view details when getting team view`() {
        controller.teamView()

        verify(twitchService, times(1)).teamViewDetails()

        verifyNoMoreInteractions(twitchService)
    }

    @Test
    fun `Should use the twitch service for tournament details when getting tournament view`() {
        controller.tournament()

        verify(twitchService, times(1)).tournamentDetails()

        verifyNoMoreInteractions(twitchService)
    }
}
