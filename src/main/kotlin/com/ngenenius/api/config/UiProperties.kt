package com.ngenenius.api.config

import com.ngenenius.api.model.platform.StreamingTab
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "ngenius.ui")
data class UiProperties(
    /**
     * A map of stream tab (on the team view) to it's properties.
     *
     * Useful since we have multiple tabs that show streaming content.
     */
    val tabs: Map<StreamingTab, TabProperties>
)

data class TabProperties(
    /**
     * Whether or not this tab is shown.
     */
    val display: Boolean
)
