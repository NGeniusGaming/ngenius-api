package com.ngenenius.api.controller

import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.service.twitch.TwitchDataService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/twitch")
class TwitchController(private val twitchDataService: TwitchDataService) {

    @GetMapping("/{tab}")
    fun detailsByTab(@PathVariable tab: StreamingTab) = twitchDataService.twitchDetails(tab)
}
