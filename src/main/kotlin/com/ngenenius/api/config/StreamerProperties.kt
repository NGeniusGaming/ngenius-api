package com.ngenenius.api.config

import com.ngenenius.api.model.platform.StreamingProvider
import com.ngenenius.api.model.platform.StreamingTab
import com.ngenenius.api.service.twitch.toQueryParams
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

interface StreamerProvider {
    fun channelNames(platform: StreamingProvider, tab: StreamingTab): Channels
}

interface TwitchStreamerProvider: StreamerProvider {

    fun twitchStreamersFor(tab: StreamingTab): Channels {
        return channelNames(StreamingProvider.TWITCH, tab)
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "ngenius.streamer")
data class StreamerProperties(val channels: List<ChannelProperties>): StreamerProvider, TwitchStreamerProvider {

    override fun channelNames(platform: StreamingProvider, tab: StreamingTab) =
        Channels(
            channels.filter{it.platform == platform}
                .filter{it.tabs.contains(tab)}
                .map{ it.id }
        )
}

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

data class Channels(val channels: List<String>)
