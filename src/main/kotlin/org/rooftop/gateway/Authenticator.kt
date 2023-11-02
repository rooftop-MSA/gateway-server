package org.rooftop.gateway

import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono

fun interface Authenticator {

    fun auth(token: String, requesterId: Long): Mono<HttpStatus>
}
