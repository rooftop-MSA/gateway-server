package org.rooftop.gateway

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.*

@Configuration
internal class RouterConfig(
    @Value("\${rooftop.server.identity.host}") private val identityHost: String,
    @Value("\${rooftop.server.identity.url}") private val identityUrl: String,
    @Value("\${rooftop.server.identity.default-url}") private val defaultUrl: String,
) {
    @Bean
    internal fun gateways(routeLocatorBuilder: RouteLocatorBuilder): RouteLocator {
        return routeLocatorBuilder.routes()
            .route("identity#create-user") {
                it.method(POST)
                    .and().host(identityHost)
                    .and().path(defaultUrl)
                    .uri(identityUrl)
            }
            .route("identity#login") {
                it.method(POST)
                    .and().host(identityHost)
                    .and().path("/v1/logins")
                    .uri(identityUrl)
            }
            .route("identity#find-user-by-name") {
                it.method(GET)
                    .and().host(identityHost)
                    .and().path(defaultUrl)
                    .and().query("name")
                    .uri(identityUrl)
            }
            .route("identity#update-user") {
                it.method(PUT)
                    .and().host(identityHost)
                    .and().path(defaultUrl)
                    .filters { it }
                    .uri(identityUrl)
            }
            .route("identity#delete-user") {
                it.method(DELETE)
                    .and().host(identityHost)
                    .and().path(defaultUrl)
                    .filters { it }
                    .uri(identityUrl)
            }
            .build()
    }
}
