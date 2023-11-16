package org.rooftop.gateway.api

import org.rooftop.api.identity.UserUpdateReq
import org.rooftop.gateway.PreAuthGatewayFilterFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.route.builder.Buildable
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR
import org.springframework.http.HttpMethod.*
import org.springframework.stereotype.Component

private lateinit var baseUrl: String
private lateinit var identityHost: String
private lateinit var identityUrl: String
private lateinit var preAuthFilter: PreAuthGatewayFilterFactory
private val notExistCachedBodyException = IllegalArgumentException("Not exist cached body")
private val notExistIdHeaderException = IllegalArgumentException("Not exist header named id")

internal enum class IdentityApiSpec(
    val routerName: String,
    private val spec: (PredicateSpec) -> Buildable<Route>,
) {

    CREATE_USER("identity#create-user", { spec ->
        spec.method(POST)
            .and().host(identityHost)
            .and().path(baseUrl)
            .uri(identityUrl)
    }),

    LOGIN("identity#login", { spec ->
        spec.method(POST)
            .and().host(identityHost)
            .and().path("/v1/logins")
            .uri(identityUrl)
    }),

    FIND_USER_BY_NAME("identity#find-user-by-name", { spec ->
        spec.method(GET)
            .and().host(identityHost)
            .and().path(baseUrl)
            .and().query("name")
            .uri(identityUrl)
    }),

    UPDATE_USER("identity#update-user", { spec ->
        spec.method(PUT)
            .and().host(identityHost)
            .and().path(baseUrl)
            .filters { filterSpec ->
                filterSpec.cacheRequestBody(ByteArray::class.java)
                filterSpec.filter(
                    preAuthFilter.apply(
                        PreAuthGatewayFilterFactory.Config { exchange ->
                            val cachedBody: ByteArray =
                                exchange.attributes[CACHED_REQUEST_BODY_ATTR].let {
                                    if (it is ByteArray) {
                                        return@let it
                                    }
                                    throw notExistCachedBodyException
                                }
                            UserUpdateReq.parseFrom(cachedBody).id
                        }
                    )
                )
            }
            .uri(identityUrl)
    }),

    DELETE_USER("identity#delete-user", { spec ->
        spec.method(DELETE)
            .and().host(identityHost)
            .and().path(baseUrl)
            .filters { filterSpec ->
                filterSpec.filter(
                    preAuthFilter.apply(
                        PreAuthGatewayFilterFactory.Config { exchange ->
                            val idHeader =
                                exchange.request.headers["id"] ?: throw notExistIdHeaderException
                            val id = idHeader[0] ?: throw notExistIdHeaderException

                            id.toLong()
                        }
                    )
                )
            }
            .uri(identityUrl)
    }),
    ;

    fun route(predicateSpec: PredicateSpec): Buildable<Route> = spec.invoke(predicateSpec)
}

@Component
class IdentitySpecApplier(
    authFilter: PreAuthGatewayFilterFactory,
    @Value("\${rooftop.server.identity.host}") host: String,
    @Value("\${rooftop.server.identity.url}") url: String,
    @Value("\${rooftop.server.identity.default-url}") defaultUrl: String,
) {
    init {
        preAuthFilter = authFilter
        identityHost = host
        identityUrl = url
        baseUrl = defaultUrl
    }
}
