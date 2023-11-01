package org.rooftop.gateway

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.GET
import java.net.URI

@Configuration
internal class RouterConfig {

    @Bean
    internal fun gateways(routeLocatorBuilder: RouteLocatorBuilder): RouteLocator {
        return routeLocatorBuilder.routes()
            .route("sample-router") {
                it.method(GET).and().path("/sample")
                    .uri(URI.create("https://google.com"))
            }
            .build()
    }
}
