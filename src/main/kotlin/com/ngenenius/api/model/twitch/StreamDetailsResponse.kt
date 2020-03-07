package com.ngenenius.api.model.twitch

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class StreamDetailsResponse(
    val data: List<StreamDetails>,
    val pagination: TwitchPagination
)

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
        val tagIds: List<String>
        )

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TwitchPagination(val cursor: String = "")