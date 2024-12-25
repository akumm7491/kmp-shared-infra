package com.example.kmp.messaging

import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Factory for creating messages
 */
object MessageFactory {
    val json: Json = Json { 
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    /**
     * Creates a Message from string payload
     */
    fun createMessage(
        payload: String,
        type: String,
        metadata: Map<String, String> = emptyMap()
    ): Message = JvmMessage(
        id = UUID.randomUUID().toString(),
        payload = payload.toByteArray(),
        headers = metadata + ("type" to type)
    )

    /**
     * Creates a Message from serializable object
     */
    inline fun <reified T> createMessage(
        value: T,
        type: String,
        metadata: Map<String, String> = emptyMap()
    ): Message {
        val payload = json.encodeToString(
            kotlinx.serialization.serializer(),
            value
        )
        return createMessage(payload, type, metadata)
    }

    /**
     * Creates a response Message
     */
    fun createResponse(
        payload: String,
        originalMessage: Message,
        metadata: Map<String, String> = emptyMap()
    ): Message = JvmMessage(
        id = UUID.randomUUID().toString(),
        payload = payload.toByteArray(),
        headers = metadata + ("type" to "response"),
        correlationId = originalMessage.id,
        replyTo = originalMessage.replyTo
    )
}

/**
 * JVM implementation of MessageBuilder
 */
class JvmMessageBuilder : MessageBuilder {
    private var id: String = UUID.randomUUID().toString()
    private var payload: ByteArray = ByteArray(0)
    private var headers: MutableMap<String, String> = mutableMapOf()
    private var timestamp: Long = System.currentTimeMillis()
    private var correlationId: String? = null
    private var replyTo: String? = null

    override fun setPayload(payload: ByteArray): MessageBuilder {
        this.payload = payload
        return this
    }

    override fun addHeader(key: String, value: String): MessageBuilder {
        headers[key] = value
        return this
    }

    override fun setCorrelationId(correlationId: String): MessageBuilder {
        this.correlationId = correlationId
        return this
    }

    override fun setReplyTo(replyTo: String): MessageBuilder {
        this.replyTo = replyTo
        return this
    }

    override fun build(): Message = JvmMessage(
        id = id,
        payload = payload,
        headers = headers,
        timestamp = timestamp,
        correlationId = correlationId,
        replyTo = replyTo
    )
}
