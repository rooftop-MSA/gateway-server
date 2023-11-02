package org.rooftop.gateway

import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import javax.security.sasl.AuthenticationException

@Service
class PreAuthGatewayFilterFactory(private val authenticator: Authenticator) :
    AbstractGatewayFilterFactory<PreAuthGatewayFilterFactory.Config>() {

    override fun apply(config: Config): GatewayFilter {
        return GatewayFilter { exchange, chain ->
            val token = exchange.request.headers[HttpHeaders.AUTHORIZATION]?.get(0)
                ?: throw emptyAuthHeaderException
            val requesterId = config.requesterIdSpec.invoke(exchange)

            authenticator.auth(token, requesterId)
                .onErrorMap { authFailException }
                .filterWhen { isAuthFail(it) }
                .doOnNext { exchange.response.setStatusCode(HttpStatus.UNAUTHORIZED) }
                .map { throw authFailException }

            chain.filter(exchange)
        }
    }

    private fun isAuthFail(httpStatus: HttpStatus): Mono<Boolean> {
        return Mono.just(!HttpStatus.OK.isSameCodeAs(httpStatus))
    }

    class Config(val requesterIdSpec: (ServerWebExchange) -> Long)

    private companion object Exceptions {
        private val emptyAuthHeaderException =
            IllegalStateException("HttpHeaders.AUTHORIZATION must be set to use this filter")
        private val authFailException =
            AuthenticationException("Authenticate failed by identity server")
    }
}
