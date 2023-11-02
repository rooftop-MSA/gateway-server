package org.rooftop.gateway

import org.rooftop.gateway.api.IdentityApiSpec
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
internal class RouterConfig {
    @Bean
    internal fun gateways(routeLocatorBuilder: RouteLocatorBuilder): RouteLocator {
        return routeLocatorBuilder.routes()
            .route(IdentityApiSpec.CREATE_USER.routerName) {
                IdentityApiSpec.CREATE_USER.route(it)
            }
            .route(IdentityApiSpec.LOGIN.routerName) {
                IdentityApiSpec.LOGIN.route(it)
            }
            .route(IdentityApiSpec.FIND_USER_BY_NAME.routerName) {
                IdentityApiSpec.FIND_USER_BY_NAME.route(it)
            }
            .route(IdentityApiSpec.UPDATE_USER.routerName) {
                IdentityApiSpec.UPDATE_USER.route(it)
            }
            .route(IdentityApiSpec.DELETE_USER.routerName) {
                IdentityApiSpec.DELETE_USER.route(it)
            }
            .build()
    }
}
