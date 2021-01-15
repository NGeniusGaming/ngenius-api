package com.ngenenius.api.controller

import com.ngenenius.api.service.TwitchStreamsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/twitch")
class TwitchBetaController(private val twitchStreamsService: TwitchStreamsService) {

    @GetMapping("/team-view")
    fun teamView() = twitchStreamsService.teamViewDetails()

    @GetMapping("/tournament")
    fun tournament() = twitchStreamsService.tournamentDetails()
}
