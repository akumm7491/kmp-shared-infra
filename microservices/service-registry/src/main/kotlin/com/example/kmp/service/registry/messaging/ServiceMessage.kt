package com.example.kmp.service.registry.messaging

import com.example.kmp.messaging.Message
import com.example.kmp.service.registry.model.ServiceInstance
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID

/**
 * Service registry specific message implementation.
 * Handles serialization of service events including:
 * - Service registration
 * - Service updates
 * - Status changes
 * - Heartbeats
 */
class ServiceMessage(
    private val content: Any,
    private val additionalHeaders: Map<String, String> = emptyMap(),
    private val messageCorrelationId: String? = null,
    private val messageReplyTo: String? = null
) : Message {
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }

    override val id: String = UUID.randomUUID().toString()
    
    override val payload: ByteArray = when (content) {
        is ServiceInstance -> json.encodeToString(content).toByteArray()
        is Map<*, *> -> {
            val jsonObject = JsonObject(content.mapKeys { it.key.toString() }
                .mapValues { entry ->
                    when (val value = entry.value) {
                        is String -> JsonPrimitive(value)
                        is Number -> JsonPrimitive(value)
                        is Boolean -> JsonPrimitive(value)
                        null -> JsonPrimitive("")
                        else -> JsonPrimitive(value.toString())
                    }
                })
            json.encodeToString(jsonObject).toByteArray()
        }
        else -> throw IllegalArgumentException("Unsupported content type: ${content.javaClass}")
    }
    
    override val headers: Map<String, String> = buildMap {
        putAll(additionalHeaders)
        put("contentType", when (content) {
            is ServiceInstance -> "application/vnd.service-instance+json"
            is Map<*, *> -> "application/json"
            else -> "application/octet-stream"
        })
        put("timestamp", System.currentTimeMillis().toString())
    }

    override val timestamp: Long = System.currentTimeMillis()
    override val correlationId: String? = messageCorrelationId
    override val replyTo: String? = messageReplyTo
}
