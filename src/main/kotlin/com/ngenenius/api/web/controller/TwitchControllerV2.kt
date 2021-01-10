package com.ngenenius.api.web.controller

import com.ngenenius.api.web.service.TwitchServiceV2
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/twitch")
class TwitchControllerV2(private val twitchServiceV2: TwitchServiceV2) {

    @GetMapping("/team-view")
    fun teamView() = twitchServiceV2.teamViewDetails()

    @GetMapping("/tournament")
    fun tournament() = twitchServiceV2.tournamentDetails()
}
