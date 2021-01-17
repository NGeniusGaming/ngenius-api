package com.ngenenius.api

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(
    properties = [
        "TWITCH_CLIENT_ID=my-twitch-client-id",
        "TWITCH_CLIENT_SECRET=my-twitch-client-secret"
    ]
)
internal class NgeniusApiApplicationTest {

    @Test
    fun `Spring Boot Context should load`() {

    }
}
