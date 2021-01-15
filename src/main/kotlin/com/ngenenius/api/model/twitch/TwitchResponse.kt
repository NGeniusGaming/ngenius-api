package com.ngenenius.api.model.twitch

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * A generic wrapper for twitch responses, which generally contain a
 * data attribute with the response contents, and a pagination reference
 * in order to retrieve all of the results to your desired operation.
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TwitchResponse<RESPONSE_TYPE>(
    val data: List<RESPONSE_TYPE>,
    val pagination: TwitchPagination
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TwitchPagination(val cursor: String = "")
