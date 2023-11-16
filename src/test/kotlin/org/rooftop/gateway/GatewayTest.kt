package org.rooftop.gateway

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import org.rooftop.api.identity.userUpdateReq
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.DefaultUriBuilderFactory

@AutoConfigureWireMock(port = 8081)
@ContextConfiguration(classes = [TestConfiguration::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
internal class GatewayTest(
    private val wireMockServer: WireMockServer,
    @LocalServerPort private val port: Int,
) : FunSpec({

    val webClient = WebTestClient.bindToServer()
        .baseUrl("http://localhost:$port").build();

    beforeSpec {
        wireMockServer.stubFor(
            WireMock.any(WireMock.anyUrl())
                .willReturn(
                    WireMock.aResponse().withStatus(ROUTE_SUCCEED_STATUS)
                        .withHeader(routeSucceedHeader.first, routeSucceedHeader.second)
                )
        )
    }

    context("Routing 가능한 URL이 주어지면, Routing을 성공한다 반환한다.") {
        withData(nameFn = { it.description }, apis) {
            val client =
                webClient.method(it.method)
                    .uri(
                        DefaultUriBuilderFactory().builder()
                            .scheme("http")
                            .host("localhost")
                            .path(it.path)
                            .port(port)
                            .query(it.param)
                            .build()
                    )

            it.headers.forEach { (header, value) -> client.header(header, value) }

            it.body?.let { body ->
                client.bodyValue(body)
            }

            client.exchange()
                .expectStatus().isEqualTo(ROUTE_SUCCEED_STATUS)
                .expectHeader().valueEquals(routeSucceedHeader.first, routeSucceedHeader.second)
        }
    }
}) {
    companion object TestSuite {
        private val routeSucceedHeader = "Routed" to "succeed"
        private const val ROUTE_SUCCEED_STATUS = 404
        private const val IDENTITY_SERVER_HOST = "localhost"

        private val apis: List<Api> = listOf(
            Api(POST, IDENTITY_SERVER_HOST, "/v1/users", description = "create user"),
            Api(POST, IDENTITY_SERVER_HOST, "/v1/logins", description = "login"),
            Api(
                GET,
                IDENTITY_SERVER_HOST,
                "/v1/users",
                param = "name=hello_msa",
                description = "get user by name"
            ),
            Api(
                PUT,
                IDENTITY_SERVER_HOST,
                "/v1/users",
                headers = mapOf(
                    HttpHeaders.AUTHORIZATION to "jwt.jwt.jwt",
                    HttpHeaders.CONTENT_TYPE to "application/x-protobuf"
                ),
                description = "update user",
                body = userUpdateReq {
                    id = 1
                    newName = "newName"
                    newPassword = "newPassword"
                }.toByteArray()
            ),
            Api(
                DELETE,
                IDENTITY_SERVER_HOST,
                "/v1/users",
                headers = mapOf(HttpHeaders.AUTHORIZATION to "jwt.jwt.jwt", "id" to "1"),
                description = "delete user"
            ),
        )
    }

    private data class Api(
        val method: HttpMethod,
        val host: String,
        val path: String,
        val param: String = "",
        val headers: Map<String, String> = mapOf(),
        val description: String = "",
        val body: ByteArray? = null,
    )
}
