/**
 * A collection of objects that are returned as the 'data' parameter
 * in a [TwitchResponse] from the Twitch public API.
 */
package com.ngenenius.api.model.twitch

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * The data object for [the Get Streams API](https://dev.twitch.tv/docs/api/reference#get-streams)
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class StreamDetails(
    val id: String,
    val userId: String,
    val userName: String,
    val gameId: String,
    val gameName: String,
    val type: String,
    val title: String,
    val viewerCount: Int,
    val startedAt: String,
    val language: String,
    val thumbnailUrl: String,
    val tagIds: Collection<String> = listOf()
)

/**
 * The data object for [the Get Users API](https://dev.twitch.tv/docs/api/reference#get-users)
 *
 * The email param is intentionally left off.  We don't need PII.
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class UserDetails(
    val id: String,
    val broadcasterType: BroadcasterType,
    val description: String,
    val displayName: String,
    val login: String,
    val offlineImageUrl: String,
    val profileImageUrl: String,
    val type: UserType,
    val viewCount: Long,
    val createdAt: String
)
