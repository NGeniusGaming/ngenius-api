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
    /**
     * The configuration for the team-view twitch data
     */
    @NestedConfigurationProperty val teamView: TwitchStreamsProperties,
    /**
     * The configuration for the tournament view twitch data
     */
    @NestedConfigurationProperty val tournament: TwitchStreamsProperties
)

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

data class TwitchStreamsProperties(
    /**
     * The channels list to query for in this twitch streams container.
     */
    val channels: List<String>,
) {
    /**
     * Helper function to convert a list of channels to the query params string the Twitch API expects.
     */
    fun channelsAsQueryParams() = channels.joinToString("&") { "user_login=$it" }
}
