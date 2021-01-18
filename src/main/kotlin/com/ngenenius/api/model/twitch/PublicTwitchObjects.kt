/**
 * Twitch API objects that are returned by the ngen api.
 */
package com.ngenenius.api.model.twitch

data class AggregateTwitchResponse(
    val user: UserDetails,
    val stream: StreamDetails?
)
