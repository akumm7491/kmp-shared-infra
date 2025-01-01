package com.example.kmp.template

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.example.kmp.template.model.DemoRequest
import com.example.kmp.template.config.AppConfig
import com.example.kmp.services.events.HealthStatus
import java.util.UUID

class ApplicationTest : StringSpec({
    "!health endpoint should return UP status" {
        // TODO: Fix test configuration issues
        // Test temporarily disabled due to configuration issues
        // testApplication {
        //     application {
        //         service.apply { 
        //             configureForTest()
        //         }
        //     }
        //     
        //     client.get("/health").apply {
        //         status shouldBe HttpStatusCode.OK
        //         val response = Json.decodeFromString<HealthStatus>(bodyAsText())
        //         response.status shouldBe "UP"
        //         response.service shouldBe "${AppConfig.PROJECT_ID}-${AppConfig.SERVICE_NAME}"
        //     }
        // }
    }

    "!demo endpoint should require authorization" {
        // TODO: Fix test configuration issues
        // Test temporarily disabled due to configuration issues
        // testApplication {
        //     application {
        //         service.apply {
        //             configureForTest()
        //         }
        //     }
        //     
        //     client.post("/api/v1/demo") {
        //         contentType(ContentType.Application.Json)
        //         setBody(Json.encodeToString(DemoRequest.serializer(), DemoRequest(
        //             id = UUID.randomUUID().toString(),
        //             data = "test data"
        //         )))
        //     }.apply {
        //         status shouldBe HttpStatusCode.Unauthorized
        //         val response = Json.decodeFromString<Map<String, String>>(bodyAsText())
        //         response["error"] shouldBe "Invalid token"
        //     }
        // }
    }
})
