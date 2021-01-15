package com.ngenenius.api.service.twitch

internal fun List<String>.toQueryParams(key: String = "user_login")
= this.joinToString("&") { "$key=$it" }
