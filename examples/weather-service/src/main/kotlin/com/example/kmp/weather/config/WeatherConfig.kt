package com.example.kmp.weather.config

import com.example.kmp.networking.models.ServiceConfig

object WeatherConfig {
    // Producer service config
    val port = System.getenv("PORT")?.toInt() ?: 8084
    val host = System.getenv("HOST") ?: "weather-service"
    
    val serviceName = System.getenv("APPLICATION_NAME") ?: "WEATHER-SERVICE"
    val serviceConfig = ServiceConfig(
        serviceName = serviceName,
        serviceUrl = "http://${System.getenv("HOST") ?: "weather-service"}:$port",
        registryUrl = System.getenv("REGISTRY_URL") ?: "http://localhost:8761/eureka",
        configServerUrl = System.getenv("CONFIG_SERVER_URL") ?: "http://localhost:8888",
        instanceId = "${System.getenv("HOST") ?: "weather-service"}:$serviceName:$port",
        vipAddress = serviceName,
        secureVipAddress = serviceName
    )
    
    // Consumer service config
    val consumerPort = System.getenv("CONSUMER_PORT")?.toInt() ?: 8085
    val consumerServiceName = System.getenv("CONSUMER_APPLICATION_NAME") ?: "WEATHER-CONSUMER"
    
    val consumerServiceConfig = ServiceConfig(
        serviceName = consumerServiceName,
        serviceUrl = "http://${System.getenv("HOST") ?: "weather-consumer"}:$consumerPort",
        registryUrl = System.getenv("REGISTRY_URL") ?: "http://localhost:8761/eureka",
        configServerUrl = System.getenv("CONFIG_SERVER_URL") ?: "http://localhost:8888",
        instanceId = "${System.getenv("HOST") ?: "weather-consumer"}:$consumerServiceName:$consumerPort",
        vipAddress = consumerServiceName,
        secureVipAddress = consumerServiceName
    )
}
