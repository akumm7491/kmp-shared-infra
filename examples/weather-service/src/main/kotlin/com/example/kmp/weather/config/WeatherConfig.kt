package com.example.kmp.weather.config

import com.example.kmp.networking.models.ServiceConfig

object WeatherConfig {
    // Producer service config
    val port = System.getenv("PORT")?.toInt() ?: 8084
    val host = System.getenv("HOST") ?: "0.0.0.0"
    
    val serviceConfig = ServiceConfig(
        serviceName = System.getenv("SPRING_APPLICATION_NAME") ?: "weather-service",
        serviceUrl = "http://$host:$port",
        registryUrl = System.getenv("EUREKA_URL") ?: "http://localhost:8761/eureka",
        configServerUrl = System.getenv("CONFIG_SERVER_URL") ?: "http://localhost:8888"
    )
    
    // Consumer service config
    val consumerPort = System.getenv("CONSUMER_PORT")?.toInt() ?: 8085
    
    val consumerServiceConfig = ServiceConfig(
        serviceName = System.getenv("CONSUMER_APPLICATION_NAME") ?: "weather-consumer",
        serviceUrl = "http://$host:$consumerPort",
        registryUrl = System.getenv("EUREKA_URL") ?: "http://localhost:8761/eureka",
        configServerUrl = System.getenv("CONFIG_SERVER_URL") ?: "http://localhost:8888"
    )
}
