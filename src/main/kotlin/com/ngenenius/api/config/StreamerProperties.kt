package com.ngenenius.api.config

import com.ngenenius.api.model.platform.StreamingProvider
import com.ngenenius.api.model.platform.StreamingTab
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "ngenius.streamer")
data class StreamerProperties(val channels: List<ChannelProperties>)

data class ChannelProperties(
    /**
     * The name of the channel / common identifier.
     */
    val id: String,
    /**
     * The platform this channel lives on.
     */
    val platform: StreamingProvider,
    /**
     * The tab(s) to show this channel on.
     */
    val tabs: List<StreamingTab>
)
