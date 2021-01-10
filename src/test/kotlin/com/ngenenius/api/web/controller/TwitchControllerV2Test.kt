package com.ngenenius.api.web.controller

import com.ngenenius.api.web.service.TwitchServiceV2
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.jupiter.api.Test

internal class TwitchControllerV2Test {

    private val twitchService = mock<TwitchServiceV2>()
    private val controller = TwitchControllerV2(twitchService)

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
