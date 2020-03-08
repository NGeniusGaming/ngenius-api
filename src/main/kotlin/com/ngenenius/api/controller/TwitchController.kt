package com.ngenenius.api.controller

import com.ngenenius.api.model.twitch.StreamDetailsResponse
import com.ngenenius.api.service.TwitchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/twitch")
class TwitchController(private val twitchService: TwitchService) {

    @GetMapping("/team-view")
    fun teamViewStreamDetails(): Mono<StreamDetailsResponse> {
        return twitchService.teamViewStreamDetails()
    }

    @GetMapping("/tournament")
    fun tournamentStreamDetails(): Mono<StreamDetailsResponse> {
        return twitchService.tournamentViewStreamDetails()
    }
}