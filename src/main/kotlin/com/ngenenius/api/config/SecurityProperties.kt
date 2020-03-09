package com.ngenenius.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * All security related properties specfic to the ngen-api.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "ngenius.security")
data class SecurityProperties(
    /**
     * The cors configuration properties
     */
    @NestedConfigurationProperty val cors: CorsProperties
)

data class CorsProperties(
    /**
     * The allowed domains
     */
    val allowedDomains: List<String>
)
