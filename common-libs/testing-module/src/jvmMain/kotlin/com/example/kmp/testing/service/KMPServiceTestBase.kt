package com.example.kmp.testing.service

import com.example.kmp.testing.base.KMPTestBase
import com.example.kmp.auth.AuthProvider
import com.example.kmp.monitoring.MetricsProvider
import com.example.kmp.messaging.MessageBroker
import com.example.kmp.storage.StorageProvider
import io.ktor.server.testing.*
import io.mockk.mockk
import io.mockk.every
import io.mockk.verify
import kotlinx.serialization.json.Json
import arrow.core.Either
import kotlinx.coroutines.runBlocking
import io.mockk.coEvery
import com.example.kmp.testing.config.KMPTestConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.routing.routing
import java.net.ServerSocket
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.slf4j.LoggerFactory
import io.ktor.serialization.kotlinx.json.*

/**
 * Base class for KMP service tests.
 * Provides service-specific testing functionality:
 * - Mock dependencies
 * - Service configuration
 * - Common test scenarios
 * - Service validation
 * - Test server setup
 */
abstract class KMPServiceTestBase : KMPTestBase() {
    // Common mocks
    protected val mockAuth = mockk<AuthProvider>(relaxed = true)
    protected val mockMonitoring = mockk<MetricsProvider>(relaxed = true)
    protected val mockMessaging = mockk<MessageBroker>(relaxed = true)
    protected val mockStorage = mockk<StorageProvider>(relaxed = true)

    // JSON serialization
    protected open val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    /**
     * Get the test configuration
     */
    abstract val config: KMPTestConfig

    /**
     * Logger for tests
     */
    protected val testLogger = LoggerFactory.getLogger(this::class.java)

    // Private backing field for client
    private var _client: HttpClient? = null
    
    /**
     * Test HTTP client
     */
    protected var client: HttpClient
        get() = _client ?: throw IllegalStateException("Client not initialized")
        set(value) {
            _client = value
        }

    /**
     * Configure the test environment
     */
    protected open fun configureTestEnvironment() {
        // Base configuration, can be overridden by subclasses
    }

    /**
     * Cleanup the test environment
     */
    protected open fun cleanupTestEnvironment() {
        _client?.close()
        _client = null
    }

    /**
     * Setup before each test
     */
    @BeforeEach
    protected open fun setupTestCase(testInfo: TestInfo) {
        testLogger.info("Setting up test case: ${testInfo.displayName}")
        configureTestEnvironment()
    }

    /**
     * Cleanup after each test
     */
    @AfterEach
    protected open fun cleanupTestCase(testInfo: TestInfo) {
        testLogger.info("Cleaning up test case: ${testInfo.displayName}")
        cleanupTestEnvironment()
    }

    /**
     * Configure service for testing
     */
    protected abstract suspend fun configureService(): suspend Application.() -> Unit

    /**
     * Run service test
     */
    protected suspend fun withService(test: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication {
            val testPort = findAvailablePort()
            environment {
                config = MapApplicationConfig().apply {
                    put("ktor.deployment.port", testPort.toString())
                    put("ktor.deployment.host", "localhost")
                }
            }
            
            application {
                runBlocking {
                    val configure = configureService()
                    configure()
                }
            }
            
            _client?.close()
            _client = createClient {
                install(ClientContentNegotiation) {
                    json(json)
                }
                install(Logging) {
                    level = LogLevel.INFO
                    logger = object : Logger {
                        override fun log(message: String) {
                            testLogger.info(message)
                        }
                    }
                }
                defaultRequest {
                    url {
                        host = "localhost"
                        port = testPort
                    }
                    header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
            }
            
            try {
                test()
            } finally {
                _client?.close()
                _client = null
            }
        }
    }

    /**
     * Runs a test server with the given configuration.
     */
    protected suspend fun withTestServer(
        port: Int = findAvailablePort(),
        module: Application.() -> Unit,
        test: suspend HttpClient.() -> Unit
    ) {
        testApplication {
            environment {
                config = MapApplicationConfig().apply {
                    put("ktor.deployment.port", port.toString())
                    put("ktor.deployment.host", "localhost")
                }
            }
            application(module)
            val testClient = createClient {
                install(ClientContentNegotiation) {
                    json(json)
                }
                defaultRequest {
                    url {
                        host = "localhost"
                        this.port = port
                    }
                    header(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                install(Logging) {
                    level = LogLevel.INFO
                    logger = object : Logger {
                        override fun log(message: String) {
                            testLogger.info(message)
                        }
                    }
                }
            }
            try {
                _client = testClient
                test(testClient)
            } finally {
                testClient.close()
                _client = null
            }
        }
    }

    /**
     * Finds an available port for testing.
     */
    protected fun findAvailablePort(): Int = ServerSocket(0).use { it.localPort }

    /**
     * Verify service operation
     */
    protected fun verifyOperation(
        operation: String,
        success: Boolean = true,
        verify: suspend () -> Unit = {}
    ) {
        runBlocking {
            if (success) {
                verify {
                    mockMonitoring.incrementCounter(operation)
                    mockMonitoring.recordTimer("operation.time", any(), any())
                }
                verify()
            } else {
                verify {
                    mockMonitoring.incrementCounter("errors.total")
                    mockMonitoring.incrementCounter("errors.$operation")
                }
            }
        }
    }

    /**
     * Test service scenario
     */
    protected suspend fun serviceScenario(
        name: String,
        setup: suspend () -> Unit = {},
        test: suspend () -> Unit,
        verify: suspend () -> Unit
    ) {
        scenario(name) {
            setup()
            step("Execute") {
                test()
            }
            step("Verify") {
                verify()
            }
        }
    }

    /**
     * Test service error handling
     */
    protected suspend fun errorScenario(
        name: String,
        error: Throwable,
        operation: String,
        test: suspend () -> Unit
    ) {
        serviceScenario(
            name = "Error: $name",
            setup = {
                runTest {
                    coEvery { mockStorage.write(any<String>(), any<String>()) } throws error
                }
            },
            test = test,
            verify = {
                verify {
                    mockMonitoring.incrementCounter("errors.total")
                    mockMonitoring.incrementCounter("errors.$operation")
                }
            }
        )
    }

    /**
     * Test service performance
     */
    protected suspend fun performanceTest(
        name: String,
        iterations: Int = 100,
        test: suspend () -> Unit
    ) {
        val times = mutableListOf<Long>()

        serviceScenario(
            name = "Performance: $name",
            test = {
                repeat(iterations) {
                    val start = System.nanoTime()
                    test()
                    val end = System.nanoTime()
                    times.add(end - start)
                }
            },
            verify = {
                val average = times.average()
                val max = times.maxOrNull() ?: 0
                val min = times.minOrNull() ?: 0

                verify {
                    mockMonitoring.recordGauge("performance.test.$name.avg", average, any())
                    mockMonitoring.recordGauge("performance.test.$name.max", max.toDouble(), any())
                    mockMonitoring.recordGauge("performance.test.$name.min", min.toDouble(), any())
                }
            }
        )
    }

    /**
     * Test service validation
     */
    protected suspend fun <T> validationTest(
        name: String,
        data: T,
        validator: suspend (T) -> Either<String, T>
    ) {
        serviceScenario(
            name = "Validation: $name",
            test = {
                validator(data)
            },
            verify = {
                verify {
                    mockMonitoring.incrementCounter("validation.total")
                }
            }
        )
    }

    /**
     * Test service metrics
     */
    protected suspend fun metricsTest(
        name: String,
        metrics: Map<String, Double>,
        test: suspend () -> Unit
    ) {
        serviceScenario(
            name = "Metrics: $name",
            test = {
                test()
            },
            verify = {
                metrics.forEach { (metric, value) ->
                    verify {
                        mockMonitoring.recordGauge(metric, value, any())
                    }
                }
            }
        )
    }
}
