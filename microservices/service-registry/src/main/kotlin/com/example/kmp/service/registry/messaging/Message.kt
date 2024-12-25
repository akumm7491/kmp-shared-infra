package com.example.kmp.service.registry.messaging

import kotlinx.serialization.Serializable

/**
 * Message model for service registry events
 */
@Serializable
data class Message(
    val type: String,
    val payload: Map<String, String>
)

/**
 * Extension function to convert any object to a Message
 */
inline fun <reified T> T.toMessage(type: String): Message = Message(
    type = type,
    payload = when (this) {
        is Map<*, *> -> this.entries.associate { (k, v) -> k.toString() to v.toString() }
        else -> this.toString().let { mapOf("value" to it) }
    }
)
