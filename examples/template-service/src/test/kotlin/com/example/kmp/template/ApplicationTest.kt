package com.example.kmp.template

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json

class ApplicationTest : StringSpec({
    "health endpoint should return UP status" {
        testApplication {
            application {
                module()
            }
            
            client.get("/health").apply {
                status shouldBe HttpStatusCode.OK
                val response = Json.decodeFromString<Map<String, String>>(bodyAsText())
                response["status"] shouldBe "UP"
            }
        }
    }

    "demo endpoint should require authorization" {
        testApplication {
            application {
                module()
            }
            
            client.get("/demo").apply {
                status shouldBe HttpStatusCode.OK
                val response = Json.decodeFromString<Map<String, String>>(bodyAsText())
                response["error"] shouldBe "Invalid token"
            }
        }
    }
})
