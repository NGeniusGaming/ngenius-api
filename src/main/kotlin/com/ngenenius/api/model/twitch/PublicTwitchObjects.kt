/**
 * Twitch API objects that are returned by the ngen api.
 */
package com.ngenenius.api.model.twitch

data class AggregateTwitchResponse(
    /**
     * The Twitch user with basic details about their channel.
     */
    val user: UserDetails,
    /**
     * Details about a current live stream.
     */
    val stream: StreamDetails?
) {
    /**
     * Whether or not the user is live streaming.
     */
    val live = stream != null
}
