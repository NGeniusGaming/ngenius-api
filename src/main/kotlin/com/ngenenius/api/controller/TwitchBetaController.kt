package com.ngenenius.api.controller

import com.ngenenius.api.service.TwitchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/twitch")
class TwitchBetaController(private val twitchService: TwitchService) {

    @GetMapping("/team-view")
    fun teamView() = twitchService.teamViewDetails()

    @GetMapping("/tournament")
    fun tournament() = twitchService.tournamentDetails()
}
