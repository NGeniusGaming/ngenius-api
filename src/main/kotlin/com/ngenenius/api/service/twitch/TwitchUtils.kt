package com.ngenenius.api.service.twitch

import com.ngenenius.api.config.TwitchIdentifier

@Deprecated("Replace with one on List<TwitchIdentifer>")
internal fun List<String>.toQueryParams(key: String) = this.joinToString("&") { "$key=$it" }

internal fun Collection<TwitchIdentifier>.toQueryParams(prefix: String = "") = this.joinToString( "&" ) { it.asQueryParameter(prefix) }
