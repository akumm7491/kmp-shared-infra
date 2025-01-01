package com.example.kmp.template.config

import com.example.kmp.networking.models.ServiceConfig

/**
 * Application-wide configuration constants
 */
object AppConfig {
    val PROJECT_ID = System.getenv("APPLICATION_NAME")?.lowercase() ?: "kmp-demo"
    val SERVICE_NAME = System.getenv("APPLICATION_NAME")?.lowercase() ?: "template-service"
    val PORT = System.getenv("PORT")?.toIntOrNull() ?: 8086
    val HOST = System.getenv("HOST") ?: "template-service"
    
    object Registry {
        val URL = System.getenv("REGISTRY_URL") ?: "http://service-registry:8761"
        val CONFIG_URL = System.getenv("CONFIG_SERVER_URL") ?: "http://config-server:8888"
        val SERVICE_URL = System.getenv("SERVICE_URL") ?: "http://$HOST:$PORT"
        
        val serviceConfig = ServiceConfig(
            serviceName = SERVICE_NAME,
            serviceUrl = SERVICE_URL,
            registryUrl = URL,
            configServerUrl = CONFIG_URL,
            instanceId = "$SERVICE_NAME-${System.currentTimeMillis()}",
            vipAddress = SERVICE_NAME,
            secureVipAddress = SERVICE_NAME,
            preferIpAddress = true
        )
    }
    
    object Storage {
        const val REQUESTS_STORE = "demo-requests"
        const val RESPONSES_STORE = "demo-responses"
        val CONNECTION_STRING = System.getenv("MONGODB_CONNECTION_STRING") ?: "mongodb://localhost:27017"
    }
    
    object Messaging {
        const val DEMO_EVENTS_TOPIC = "demo.events"
        val KAFKA_SERVERS = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "kafka:29092"
        val SCHEMA_REGISTRY = System.getenv("SCHEMA_REGISTRY_URL") ?: "http://schema-registry:8081"
        val BROKER_URL = KAFKA_SERVERS
    }
    
    object Auth {
        const val JWT_ISSUER = "kmp-auth"
        const val JWT_AUDIENCE = "kmp-services"
        val SERVER_URL = System.getenv("AUTH_SERVER_URL") ?: "http://auth-service:8081"
        val CLIENT_SECRET = System.getenv("AUTH_CLIENT_SECRET") ?: "dev-secret"
    }
} 