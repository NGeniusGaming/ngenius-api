package com.ngenenius.api.controller

import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.StreamDetails
import com.ngenenius.api.model.twitch.TwitchResponse
import com.ngenenius.api.model.twitch.UserDetails
import com.ngenenius.api.service.twitch.TwitchStreamsService
import com.ngenenius.api.service.twitch.TwitchUsersService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/twitch")
class TwitchBetaController(
    private val twitchStreamsService: TwitchStreamsService,
    private val twitchUsersService: TwitchUsersService
) {

    @GetMapping("/team-view")
    internal fun teamView(): TwitchResponse<StreamDetails> =
        TwitchResponse(twitchStreamsService.teamViewDetails())

    @GetMapping("/tournament")
    internal fun tournament(): TwitchResponse<StreamDetails> =
        TwitchResponse(twitchStreamsService.tournamentDetails())

    @GetMapping("/users")
    internal fun users(): Collection<UserDetails> = twitchUsersService.users(StreamingTab.TEAM_VIEW)


}
