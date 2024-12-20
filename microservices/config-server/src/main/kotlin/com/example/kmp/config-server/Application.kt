package com.example.kmp.config.server

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import com.charleskorn.kaml.Yaml
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8888) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Configure JSON serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    routing {
        // Health check endpoint
        get("/actuator/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }

        get("/config/{service}/{profile}") {
            val service = call.parameters["service"] ?: throw IllegalArgumentException("Service name required")
            val profile = call.parameters["profile"] ?: "default"
            
            try {
                val config = loadConfig(service, profile)
                call.respond(HttpStatusCode.OK, config)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Configuration not found for $service-$profile")
                )
            }
        }
    }
}

private fun loadConfig(service: String, profile: String): Map<String, Any> {
    val configPath = System.getenv("CONFIG_REPO_PATH") ?: "/config-repo"
    val configFile = File("$configPath/$service/application-$profile.yml")
    
    if (!configFile.exists()) {
        // Fall back to default config
        val defaultConfig = File("$configPath/$service/application.yml")
        if (!defaultConfig.exists()) {
            throw IllegalStateException("No configuration found for $service")
        }
        return parseYamlConfig(defaultConfig)
    }
    
    return parseYamlConfig(configFile)
}

@Serializable
private data class Config(
    val properties: Map<String, JsonElement> = emptyMap()
)

private fun parseYamlConfig(file: File): Map<String, Any> {
    return try {
        val yaml = Yaml.default
        val config = yaml.decodeFromString(Config.serializer(), file.readText())
        config.properties.mapValues { (_, value) -> 
            when (value) {
                is JsonPrimitive -> value.content
                is JsonObject -> value.toString()
                is JsonArray -> value.toString()
                else -> value.toString()
            }
        }
    } catch (e: Exception) {
        mapOf("error" to "Failed to parse config: ${e.message}")
    }
}
