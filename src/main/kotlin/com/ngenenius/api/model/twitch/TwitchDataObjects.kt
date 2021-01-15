/**
 * A collection of objects that are returned as the 'data' parameter
 * in a [TwitchResponse] from the Twitch public API.
 */
package com.ngenenius.api.model.twitch

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class StreamDetails(
    val id: String,
    val userId: String,
    val userName: String,
    val gameId: String,
    val type: String,
    val title: String,
    val viewerCount: Int,
    val startedAt: String,
    val language: String,
    val thumbnailUrl: String,
    val tagIds: List<String> = listOf()
)
