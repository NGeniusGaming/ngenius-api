package com.ngenenius.api.model.platform

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * The names of the tabs that are available in the application.
 */
enum class StreamingTab {
    @JsonProperty("team-view") TEAM_VIEW,
    @JsonProperty("tournament") TOURNAMENT
}
