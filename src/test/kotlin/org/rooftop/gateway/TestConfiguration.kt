package org.rooftop.gateway

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono

@TestConfiguration
class TestConfiguration {

    @Bean
    fun authenticator(): Authenticator = Authenticator { _, _ -> Mono.just(HttpStatus.OK) }

}
