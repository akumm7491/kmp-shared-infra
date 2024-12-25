package com.example.kmp.testing

import app.cash.turbine.*
import kotlinx.coroutines.flow.collect
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.server.application.*
import io.mockk.MockKAnnotations
import kotlinx.coroutines.flow.Flow
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Utility functions for testing Ktor applications using the latest testing APIs
 */
object KtorTestUtils {
    /**
     * Test a Ktor endpoint with the given configuration
     */
    suspend fun testEndpoint(
        method: HttpMethod,
        uri: String,
        setup: Application.() -> Unit,
        requestConfig: HttpRequestBuilder.() -> Unit = {},
        assertions: suspend HttpResponse.() -> Unit
    ) {
        testApplication {
            application(setup)
            
            val response = client.request(uri) {
                this.method = method
                requestConfig()
            }
            
            assertions(response)
        }
    }

    /**
     * Common assertions for testing HTTP responses
     */
    object Assertions {
        suspend fun assertSuccess(response: HttpResponse) {
            assertTrue(response.status.isSuccess())
        }

        suspend fun assertStatusCode(response: HttpResponse, expectedStatus: HttpStatusCode) {
            assertEquals(expectedStatus, response.status)
        }

        suspend fun assertJsonContentType(response: HttpResponse) {
            assertEquals(
                ContentType.Application.Json.withCharset(Charsets.UTF_8),
                response.contentType()
            )
        }

        suspend fun assertBodyContains(response: HttpResponse, expectedContent: String) {
            assertTrue(response.bodyAsText().contains(expectedContent))
        }
    }

    /**
     * Common request configurations
     */
    object RequestConfigs {
        fun jsonRequest(content: String): HttpRequestBuilder.() -> Unit = {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(content)
        }

        fun authRequest(token: String): HttpRequestBuilder.() -> Unit = {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        fun withCookies(cookies: Map<String, String>): HttpRequestBuilder.() -> Unit = {
            cookies.forEach { (name, value) ->
                cookie(name, value)
            }
        }
    }

    /**
     * Test Flow emissions using Turbine
     */
    suspend fun <T> Flow<T>.testFlow(
        timeout: Long = 5,
        assertions: suspend Turbine<T>.() -> Unit
    ) = turbineScope {
        val turbine = Turbine<T>(timeout = timeout.seconds)
        try {
            assertions(turbine)
        } finally {
            turbine.cancelAndIgnoreRemainingEvents()
        }
    }
}

/**
 * Extension functions for common test scenarios
 */
suspend fun ApplicationTestBuilder.withJson(
    method: HttpMethod,
    uri: String,
    jsonBody: String,
    test: suspend HttpResponse.() -> Unit
) {
    client.request(uri) {
        this.method = method
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(jsonBody)
    }.apply {
        test()
    }
}

suspend fun ApplicationTestBuilder.withAuth(
    method: HttpMethod,
    uri: String,
    token: String,
    test: suspend HttpResponse.() -> Unit
) {
    client.request(uri) {
        this.method = method
        header(HttpHeaders.Authorization, "Bearer $token")
    }.apply {
        test()
    }
}

/**
 * Base class for tests that use MockK
 */
abstract class MockKTest {
    init {
        MockKAnnotations.init(this)
    }
}

/**
 * DSL for building test cases
 */
class TestBuilder {
    var method: HttpMethod = HttpMethod.Get
    var uri: String = "/"
    var setup: Application.() -> Unit = {}
    var requestConfig: HttpRequestBuilder.() -> Unit = {}
    var assertions: suspend HttpResponse.() -> Unit = {}

    suspend fun test() {
        KtorTestUtils.testEndpoint(
            method = method,
            uri = uri,
            setup = setup,
            requestConfig = requestConfig,
            assertions = assertions
        )
    }
}

suspend fun buildTest(init: TestBuilder.() -> Unit) {
    TestBuilder().apply(init).test()
}

/**
 * Example usage:
 *
 * testApplication {
 *     val client = createClient {
 *         install(ContentNegotiation) {
 *             json()
 *         }
 *     }
 *     
 *     val response = client.post("/api/users") {
 *         contentType(ContentType.Application.Json)
 *         setBody(UserRequest("test"))
 *     }
 *     
 *     assertEquals(HttpStatusCode.OK, response.status)
 *     val user = response.body<User>()
 *     assertEquals("test", user.name)
 * }
 *
 * // Testing flows with Turbine
 * userService.getUserUpdates().testFlow {
 *     awaitItem() shouldBe initialUser
 *     awaitItem() shouldBe updatedUser
 *     awaitComplete()
 * }
 */
