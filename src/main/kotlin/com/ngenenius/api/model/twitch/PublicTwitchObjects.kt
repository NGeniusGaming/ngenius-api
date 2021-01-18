/**
 * Twitch API objects that are returned by the ngen api.
 */
package com.ngenenius.api.model.twitch

import com.fasterxml.jackson.annotation.JsonGetter

data class AggregateTwitchResponse(
    /**
     * The Twitch user with basic details about their channel.
     */
    val user: UserDetails,
    /**
     * Details about a current live stream.
     */
    val stream: StreamDetails?,

    /**
     * The current index that this response has been placed.
     * Some derived / synthetic content is driven off this index.
     */
    var index: Int = -1
) {
    /**
     * Whether or not the user is live streaming.
     */
    val live = stream != null

    @JsonGetter
    fun videoUrl(): String? {
        val liveStream = stream ?: return null
        val isPlaying = index == 0
        return "https://player.twitch.tv/?channel=${liveStream.userName}&muted=${!isPlaying}&autoplay=${isPlaying}&parent={parent}"
    }

    @JsonGetter
    fun chatUrl(): String? {
        val liveStream = stream ?: return null
        return "https://www.twitch.tv/embed/${liveStream.userName}/chat?parent={parent}&darkpopout"
    }
}
