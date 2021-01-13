package com.ngenenius.api.controller

import com.ngenenius.api.config.ChannelProperties
import com.ngenenius.api.config.StreamerProperties
import com.ngenenius.api.config.TabProperties
import com.ngenenius.api.config.UiProperties
import com.ngenenius.api.model.platform.StreamingTab
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/config")
class ConfigController(
    uiProperties: UiProperties,
    streamerProperties: StreamerProperties
) {

    private val publicConfig = PublicConfig(uiProperties.tabs, streamerProperties.channels)

    @GetMapping
    fun publicConfiguration() = publicConfig
}

data class PublicConfig(val tabs: Map<StreamingTab, TabProperties>, val channels: List<ChannelProperties>)
