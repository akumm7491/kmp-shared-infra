package com.example.kmp.service.registry

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import com.example.kmp.service.registry.registry.DynamicServiceRegistry
import com.example.kmp.service.registry.config.ProjectRegistry
import com.example.kmp.service.registry.model.ServiceInstance
import com.example.kmp.service.registry.config.EurekaServerConfig
import com.example.kmp.service.registry.utils.generateDashboardHtml
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8761) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Initialize registries
    val projectRegistry = ProjectRegistry()
    val serviceRegistry = DynamicServiceRegistry(
        config = loadEurekaConfig(),
        projectRegistry = projectRegistry
    )

    install(ContentNegotiation) {
        json()
    }

    routing {
        // Health check endpoint
        get("/actuator/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }

        // Eureka dashboard
        get("/") {
            call.respondText(generateDashboardHtml(serviceRegistry), ContentType.Text.Html)
        }

        // Registry endpoints
        route("/eureka/apps") {
            post("/{application}") {
                val instance = call.receive<ServiceInstance>()
                serviceRegistry.registerService(instance)
                call.respond(HttpStatusCode.Created)
            }

            get("/{application}") {
                val appName = call.parameters["application"]!!
                val instances = serviceRegistry.getService(appName)
                call.respond(instances ?: HttpStatusCode.NotFound)
            }

            get {
                val services = serviceRegistry.getAllServices()
                call.respond(services)
            }
        }
    }

    // Start registry monitoring
    launch {
        serviceRegistry.startMonitoring()
    }
}

private fun loadEurekaConfig(): EurekaServerConfig {
    return EurekaServerConfig(
        port = 8761,
        peerEurekaNodes = listOf(), // Add peer nodes if running in cluster
        renewalPercentThreshold = 0.85,
        renewalIntervalInSecs = 30,
        enableSelfPreservation = true
    )
}
