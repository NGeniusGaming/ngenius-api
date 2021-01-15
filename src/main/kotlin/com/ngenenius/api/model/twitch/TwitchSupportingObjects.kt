/**
 * For items that are not Apex data objects returned by the Twitch API
 * but are still nice to encapsulate as strongly typed things.
 */
package com.ngenenius.api.model.twitch

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
enum class BroadcasterType {
    @JsonProperty("partner")
    PARTNER,
    @JsonProperty("affiliate")
    AFFILIATE,

    // Twitch sends this one as a blank string.
    @JsonEnumDefaultValue
    @JsonProperty("streamer")
    STREAMER
}

enum class UserType {
    @JsonProperty("staff")
    STAFF,
    @JsonProperty("admin")
    ADMIN,
    @JsonProperty("global_mod")
    GLOBAL_MOD,

    // Twitch sends this one as a blank string.
    @JsonEnumDefaultValue
    @JsonProperty("streamer")
    STREAMER
}
