package com.example.kmp.messaging

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * JVM implementation of Message interface.
 * Used for communication between services.
 */
@Serializable
data class JvmMessage(
    override val id: String,
    override val payload: ByteArray,
    override val headers: Map<String, String>,
    override val timestamp: Long = System.currentTimeMillis(),
    override val correlationId: String? = null,
    override val replyTo: String? = null
) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JvmMessage) return false

        if (id != other.id) return false
        if (!payload.contentEquals(other.payload)) return false
        if (headers != other.headers) return false
        if (timestamp != other.timestamp) return false
        if (correlationId != other.correlationId) return false
        if (replyTo != other.replyTo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (correlationId?.hashCode() ?: 0)
        result = 31 * result + (replyTo?.hashCode() ?: 0)
        return result
    }
}

/**
 * Extension function to convert any serializable object to a Message
 */
inline fun <reified T> T.toMessage(type: String): Message {
    val payload = kotlinx.serialization.json.Json.encodeToString(
        kotlinx.serialization.serializer(),
        this
    )
    return JvmMessage(
        id = java.util.UUID.randomUUID().toString(),
        payload = payload.toByteArray(),
        headers = mapOf("type" to type)
    )
}
