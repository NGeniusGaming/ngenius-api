package com.ngenenius.api.controller

import com.ngenenius.api.config.StreamerProperties
import com.ngenenius.api.config.UiProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/config")
class ConfigController(
    private val uiProperties: UiProperties,
    private val streamerProperties: StreamerProperties
) {

    val publicConfig = mapOf(
        "tabs" to uiProperties.tabs,
        "channels" to streamerProperties.channels
    )

    @GetMapping
    fun publicConfiguration() = publicConfig
}
