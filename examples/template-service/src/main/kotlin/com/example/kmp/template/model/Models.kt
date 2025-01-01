package com.example.kmp.template.model

import com.example.kmp.messaging.Message
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DemoRequest(
    @field:NotBlank(message = "ID is required")
    val id: String,
    @field:NotBlank(message = "Data is required")
    val data: String
)

@Serializable
data class DemoResponse(
    val id: String,
    val result: String
)

@Serializable
data class DemoEvent(
    override val id: String = UUID.randomUUID().toString(),
    override val correlationId: String,
    val eventType: String,
    val eventData: String,
    override val headers: Map<String, String> = mapOf("type" to "demo.event"),
    override val timestamp: Long = System.currentTimeMillis(),
    override val replyTo: String? = null
) : Message {
    override val payload: ByteArray
        get() = eventData.toByteArray()
} 