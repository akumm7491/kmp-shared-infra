package com.example.kmp.service.base

import io.ktor.server.application.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

abstract class BaseService(
    private val projectId: String,
    private val serviceName: String
) {
    fun Application.configureBase() {
        // Load project-specific config
        val config = ConfigClient("http://config-server:8888")
            .loadConfig("$projectId-$serviceName", projectId)
        
        // Register with service discovery
        install(EurekaClient) {
            serviceName = "$projectId-$serviceName"
            serviceUrl = "http://service-registry:8761/eureka"
            metadata["project"] = projectId
        }
        
        // Configure metrics
        install(MicrometerMetrics) {
            registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
            meterBinder { registry ->
                registry.config().commonTags(
                    "project", projectId,
                    "service", serviceName
                )
            }
        }
        
        // Configure custom service
        configureService(config)
    }
    
    // To be implemented by specific services
    abstract fun Application.configureService(config: Config)
} 