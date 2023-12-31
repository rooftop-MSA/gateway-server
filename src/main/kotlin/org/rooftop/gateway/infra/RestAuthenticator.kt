package org.rooftop.gateway.infra

import org.rooftop.gateway.Authenticator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
@Profile("product")
internal class RestAuthenticator(
    @Value("\${rooftop.server.identity.url}") private val upStreamUrl: String,
) : Authenticator {

    override fun auth(token: String, requesterId: Long): Mono<HttpStatus> {
        return WebClient.create("$upStreamUrl$AUTH_PATH").get()
            .headers { header ->
                header["Authorization"] = token
                header["RequesterId"] = requesterId.toString()
            }
            .exchangeToMono { Mono.just(it.statusCode()) }
            .map { HttpStatus.valueOf(it.value()) }
    }

    companion object {
        private const val AUTH_PATH = "/v1/auths"
    }
}
