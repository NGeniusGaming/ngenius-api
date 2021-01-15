package com.ngenenius.api.controller

import com.ngenenius.api.model.twitch.StreamDetails
import com.ngenenius.api.model.twitch.TwitchResponse
import com.ngenenius.api.service.twitch.TwitchStreamsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/twitch")
class TwitchBetaController(private val twitchStreamsService: TwitchStreamsService) {

    @GetMapping("/team-view")
    fun teamView(): TwitchResponse<StreamDetails> = twitchStreamsService.teamViewDetails()

    @GetMapping("/tournament")
    fun tournament(): TwitchResponse<StreamDetails> = twitchStreamsService.tournamentDetails()
}
