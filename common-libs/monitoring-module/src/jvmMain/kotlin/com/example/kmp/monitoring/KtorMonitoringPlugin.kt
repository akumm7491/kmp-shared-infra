package com.example.kmp.monitoring

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

class KtorMonitoringConfig {
    var metricsPath: String = "/metrics"
    var onError: (Throwable) -> Unit = { }
    internal var metricsProvider: MetricsProvider? = null
    internal var logProvider: LogProvider? = null
}

val KtorMonitoring = createApplicationPlugin(
    name = "KtorMonitoring",
    createConfiguration = ::KtorMonitoringConfig
) {
    val config = pluginConfig
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val metricsProvider = KtorMetricsProvider(registry)
    val logProvider = KtorLogProvider("KtorMonitoring")

    config.metricsProvider = metricsProvider
    config.logProvider = logProvider

    application.install(MicrometerMetrics) {
        this.registry = registry
        meterBinders = listOf()
    }

    application.install(CallLogging) {
        level = org.slf4j.event.Level.INFO
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val uri = call.request.uri
            val duration = call.processingTimeMillis()
            "Request: $httpMethod $uri - Status: $status - Duration: ${duration}ms"
        }
    }

    application.install(CallId) {
        header(io.ktor.http.HttpHeaders.XRequestId)
        generate { "REQUEST-${System.currentTimeMillis()}" }
        verify { it.isNotEmpty() }
    }

    application.install(StatusPages) {
        exception<Throwable> { call, cause ->
            config.onError(cause)
            logProvider.error(
                "Request failed",
                cause,
                mapOf(
                    "path" to call.request.uri,
                    "method" to call.request.httpMethod.value,
                    "status" to (call.response.status()?.value ?: 500).toString()
                )
            )
            call.respond(io.ktor.http.HttpStatusCode.InternalServerError, mapOf(
                "error" to (cause.message ?: "Internal Server Error")
            ))
        }
    }

    application.environment.monitor.subscribe(ApplicationStarted) {
        logProvider.info("Application started", mapOf())
    }

    application.environment.monitor.subscribe(ApplicationStopping) {
        logProvider.info("Application stopping", mapOf())
    }

    application.routing {
        get(config.metricsPath) {
            call.respond(registry.scrape())
        }
    }

    onCall { call ->
        metricsProvider.incrementCounter(
            "http_requests_total",
            mapOf(
                "status" to (call.response.status()?.value ?: 500).toString(),
                "method" to call.request.httpMethod.value,
                "path" to call.request.uri
            )
        )
    }
}
