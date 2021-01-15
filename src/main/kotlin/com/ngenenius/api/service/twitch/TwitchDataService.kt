package com.ngenenius.api.service.twitch

/**
 * The [TwitchDataService] is responsible for orchestrating data from the individual
 * twitch services to provide an apex data object.  This data service _is not_ responsible
 * for caching any data.  Since various parts of our Apex Twitch data will have different times
 * to live, caching should be the responsibility of the individual data components, and not
 * the orchestration layer.
 */
class TwitchDataService(private val twitchStreamsService: TwitchStreamsService) {
}
