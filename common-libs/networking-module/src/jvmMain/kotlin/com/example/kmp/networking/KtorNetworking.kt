package com.example.kmp.networking

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import com.example.kmp.monitoring.LogProvider
import com.example.kmp.monitoring.MetricsProvider
import com.example.kmp.networking.resilience.CircuitBreaker
import com.example.kmp.networking.resilience.RetryConfig
import com.example.kmp.networking.ratelimit.RateLimiter
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.slf4j.MDC
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

enum class HttpClientEngine {
    OKHTTP,
    CIO
}

class NetworkingConfig {
    var port: Int = 8080
    var host: String = "0.0.0.0"
    var baseUrl: String = ""
    var timeout: Long = 30000
    var connectTimeout: Long = 10000
    var readTimeout: Long = 30000
    var writeTimeout: Long = 30000
    var maxIdleConnections: Int = 5
    var keepAliveDuration: Long = 5 * 60 * 1000 // 5 minutes
    var retries: Int = 3
    var rateLimitRequests: Int = 100
    var rateLimitDuration = 1.seconds
    var circuitBreakerFailureThreshold: Int = 5
    var circuitBreakerResetTimeout: Long = 60000
    var enableCompression: Boolean = true
    var followRedirects: Boolean = true
    var defaultHeaders: Map<String, String> = emptyMap()
    var connectionPoolSize: Int = 1000
    var pipelineSize: Int = 20
    var clientEngine: HttpClientEngine = HttpClientEngine.OKHTTP
    internal var logProvider: LogProvider? = null
    internal var metricsProvider: MetricsProvider? = null
    internal var routingConfiguration: (Routing.() -> Unit)? = null
    internal var plugins: (Application.() -> Unit)? = null
    
    fun logging(logProvider: LogProvider) {
        this.logProvider = logProvider
    }
    
    fun metrics(metricsProvider: MetricsProvider) {
        this.metricsProvider = metricsProvider
    }

    fun routing(configuration: Routing.() -> Unit) {
        this.routingConfiguration = configuration
    }

    fun plugins(configuration: Application.() -> Unit) {
        this.plugins = configuration
    }
}

class KtorNetworking(private val config: NetworkingConfig) {
    companion object {
        private val logger = LoggerFactory.getLogger(KtorNetworking::class.java)
    }

    val client = when (config.clientEngine) {
        HttpClientEngine.OKHTTP -> createOkHttpClient()
        HttpClientEngine.CIO -> createCIOClient()
    }
    
    val circuitBreaker = CircuitBreaker(
        failureThreshold = config.circuitBreakerFailureThreshold,
        resetTimeoutMs = config.circuitBreakerResetTimeout
    )
    
    val rateLimiter = RateLimiter(
        maxRequests = config.rateLimitRequests,
        duration = config.rateLimitDuration
    )
    
    private fun createOkHttpClient() = HttpClient(OkHttp) {
        engine {
            config {
                retryOnConnectionFailure(true)
                connectTimeout(config.connectTimeout, TimeUnit.MILLISECONDS)
                readTimeout(config.readTimeout, TimeUnit.MILLISECONDS)
                writeTimeout(config.writeTimeout, TimeUnit.MILLISECONDS)
            }
        }
        setupCommonConfig()
    }
    
    private fun createCIOClient() = HttpClient(CIO) {
        engine {
            requestTimeout = config.timeout
            endpoint {
                connectTimeout = config.connectTimeout
                connectAttempts = config.retries
                keepAliveTime = config.keepAliveDuration
                maxConnectionsCount = config.connectionPoolSize
                maxConnectionsPerRoute = config.connectionPoolSize
                pipelineMaxSize = config.pipelineSize
            }
        }
        setupCommonConfig()
    }
    
    private fun HttpClientConfig<*>.setupCommonConfig() {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    if (config.logProvider != null) {
                        config.logProvider?.debug(message)
                    } else {
                        KtorNetworking.logger.info(message)
                    }
                }
            }
            level = LogLevel.INFO
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = config.timeout
            connectTimeoutMillis = config.connectTimeout
            socketTimeoutMillis = config.readTimeout
        }
        
        defaultRequest {
            config.defaultHeaders.forEach { (key, value) ->
                header(key, value)
            }
            getCorrelationId()?.let { 
                header("X-Correlation-ID", it)
            }
        }
    }
    
    private fun logMessage(message: String) {
        if (config.logProvider != null) {
            config.logProvider?.debug(message)
        } else {
            logger.info(message)
        }
    }
    
    private fun getCorrelationId(): String? = MDC.get("correlationId")
    
    suspend fun <T> executeWithResilience(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try {
            circuitBreaker.execute {
                rateLimiter.execute {
                    block()
                }
            }
        } catch (e: Exception) {
            logMessage("Request failed: ${e.message}")
            throw e
        }
    }
    
    fun install(application: Application) {
        application.install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        config.routingConfiguration?.let { rc -> application.routing(rc) }
        config.plugins?.let { pc -> with(application) { pc() } }
    }
}

object NetworkingFactory {
    fun create(configure: NetworkingConfig.() -> Unit): KtorNetworking {
        val config = NetworkingConfig().apply(configure)
        return KtorNetworking(config)
    }
}
