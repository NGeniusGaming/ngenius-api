package com.ngenenius.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * The Twitch properties container that holds all things twitch related.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "ngenius.twitch")
data class TwitchProperties(
    /**
     * The authentication properties container for twitch.
     */
    @NestedConfigurationProperty val auth: TwitchAuthProperties,
)

/**
 * The configuration holder for our twitch client id and client secret.
 */
data class TwitchAuthProperties(
    /**
     * The client id for twitch authentication.
     */
    val clientId: String,
    /**
     * The client secret for twitch authentication
     */
    val clientSecret: String
)
