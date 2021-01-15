package com.ngenenius.api.controller

import com.ngenenius.api.model.twitch.StreamDetails
import com.ngenenius.api.model.twitch.TwitchPagination
import com.ngenenius.api.model.twitch.TwitchResponse
import com.ngenenius.api.service.twitch.TwitchStreamsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/twitch")
class TwitchBetaController(private val twitchStreamsService: TwitchStreamsService) {

    companion object {
        // the twitch service does not return a [TwitchResponse], but we need one of these to wrap data in that.
        private val constantPagination = TwitchPagination()
    }

    @GetMapping("/team-view")
    internal fun teamView(): TwitchResponse<StreamDetails> = TwitchResponse(twitchStreamsService.teamViewDetails(), constantPagination)

    @GetMapping("/tournament")
    internal fun tournament(): TwitchResponse<StreamDetails> = TwitchResponse(twitchStreamsService.tournamentDetails(), constantPagination)
}
