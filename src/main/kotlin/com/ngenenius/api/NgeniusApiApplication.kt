package com.ngenenius.api

import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication(
	exclude = [
		// we need spring security, but not to secure this app. Disable it.
//		SecurityAutoConfiguration::class,
//		ManagementWebSecurityAutoConfiguration::class
	]
)
class NgeniusApiApplication

fun main(args: Array<String>) {
	runApplication<NgeniusApiApplication>(*args)
}
