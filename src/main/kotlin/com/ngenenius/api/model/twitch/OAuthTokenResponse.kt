package com.ngenenius.api.model.twitch

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@Deprecated("This is replaced with Spring OAuth2 instead.")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class OAuthTokenResponse(
    val accessToken: String,
    val expiresIn: Long,
    val tokenType: String
)
