package com.ngenenius.api.model.platform

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * The Stream Providers currently supported by this application.
 */
enum class StreamingProvider {
    @JsonProperty("twitch")
    TWITCH
}
