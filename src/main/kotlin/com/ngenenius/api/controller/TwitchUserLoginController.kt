package com.ngenenius.api.controller

import com.ngenenius.api.model.twitch.UserDetails
import com.ngenenius.api.service.twitch.TwitchUsersService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/twitch/users")
class TwitchUserLoginController(private val twitchUsersService: TwitchUsersService) {

    @GetMapping("/login/{login}")
    fun lookupUserByLogin(@PathVariable login: String): Collection<UserDetails> {
        return twitchUsersService.findUsersByLogin(login)
    }
}
