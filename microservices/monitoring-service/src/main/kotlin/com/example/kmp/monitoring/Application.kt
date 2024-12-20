package com.example.kmp.monitoring

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun main() {
    embeddedServer(Netty, port = 9090) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    
    install(MicrometerMetrics) {
        registry = prometheusMeterRegistry
    }
    
    routing {
        get("/metrics") {
            call.respondText(prometheusMeterRegistry.scrape())
        }
    }
} 