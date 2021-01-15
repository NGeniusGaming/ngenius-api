package com.ngenenius.api.service.twitch

internal fun List<String>.toQueryParams(key: String) = this.joinToString("&") { "$key=$it" }
