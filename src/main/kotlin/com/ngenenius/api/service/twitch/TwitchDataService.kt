package com.ngenenius.api.service.twitch

import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.model.twitch.AggregateTwitchResponse
import org.springframework.stereotype.Component

/**
 * The [TwitchDataService] is responsible for orchestrating data from the individual
 * twitch services to provide an apex data object.  This data service _is not_ responsible
 * for caching any data.  Since various parts of our Apex Twitch data will have different times
 * to live, caching should be the responsibility of the individual data components, and not
 * the orchestration layer.
 */
@Component
class TwitchDataService(
    private val twitchStreamsService: TwitchStreamsService,
    private val twitchUsersService: TwitchUsersService
) {

    /**
     * Combines users details with optional stream details if that user is live at the
     * time of calling this API.
     */
    fun twitchDetails(tab: StreamingTab): Collection<AggregateTwitchResponse> {
        val users = twitchUsersService.users(tab)
        val streams = twitchStreamsService.streamDetails(tab)
            .map { it.userId to it }
            .toMap()

        return users.map{ AggregateTwitchResponse(it, streams[it.id]) }
    }
}
