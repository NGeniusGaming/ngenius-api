package com.ngenenius.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication()
class NgeniusApiApplication

fun main(args: Array<String>) {
	runApplication<NgeniusApiApplication>(*args)
}
