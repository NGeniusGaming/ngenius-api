package com.ngenenius.api.config

import com.ngenenius.api.model.platform.StreamingProvider
import com.ngenenius.api.model.platform.StreamingTab
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

interface StreamerProvider {
    fun identifiers(platform: StreamingProvider, tab: StreamingTab): Collection<TwitchIdentifier>
}

interface TwitchStreamerProvider : StreamerProvider {

    fun twitchIdentifiers(tab: StreamingTab): Collection<TwitchIdentifier> {
        return identifiers(StreamingProvider.TWITCH, tab)
    }
}

@ConstructorBinding
@ConfigurationProperties(prefix = "ngenius.streamer")
data class StreamerProperties(val channels: List<ChannelProperties>) : StreamerProvider, TwitchStreamerProvider {

    override fun identifiers(platform: StreamingProvider, tab: StreamingTab) =
        channels.filter { it.platform == platform }
            .filter { it.tabs.contains(tab) }
            .map { it.twitchIdentifier }
            .toSet()
}

data class ChannelProperties(
    /**
     * The actual id for this Twitch channel
     */
    val id: String = "",
    /**
     * The _case sensitive_ display name of the channel.
     * If the case is incorrect with this, the Twitch API returns nothing.
     */
    val displayName: String = "",
    /**
     * The platform this channel lives on.
     */
    val platform: StreamingProvider,
    /**
     * The tab(s) to show this channel on.
     */
    val tabs: List<StreamingTab>
) {
    val twitchIdentifier = TwitchIdentifier(id, displayName)
}

data class TwitchIdentifier(val id: String = "", val displayName: String = "") {

    private val internalKey = asQueryParameter("key_")

    fun asQueryParameter(prefix: String = ""): String {
        val param = when {
            displayName.isNotBlank() -> "login=$displayName"
            id.isNotBlank() -> "id=$id" // TODO: after config migration, this should be higher priority.
            else -> throw IllegalStateException("Both the id and display-name for a Twitch Identifier is blank!")
        }

        return "$prefix$param"
    }

    /**
     * Rely on the internal key to determine equality
     */
    override fun equals(other: Any?): Boolean {
        return (other as? TwitchIdentifier)?.internalKey?.equals(internalKey) ?: false
    }

    /**
     * Rely on the internal key to compute the hashcode.
     */
    override fun hashCode() = internalKey.hashCode()

}
