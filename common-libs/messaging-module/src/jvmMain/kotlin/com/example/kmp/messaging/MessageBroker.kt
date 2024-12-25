package com.example.kmp.messaging

import java.util.UUID

/**
 * Default implementation of Message interface
 */
data class DefaultMessage(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ByteArray,
    override val headers: Map<String, String> = emptyMap(),
    override val timestamp: Long = System.currentTimeMillis(),
    override val correlationId: String? = null,
    override val replyTo: String? = null
) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefaultMessage) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Kafka implementation of MessageBroker
 */
class KafkaMessageBroker(private val config: MessagingConfig) : MessageBroker {
    override fun publish(topic: String, message: Message) {
        // TODO: Implement with Kafka
    }

    override fun subscribe(topic: String, handler: MessageHandler) {
        // TODO: Implement with Kafka
    }

    override fun unsubscribe(topic: String) {
        // TODO: Implement with Kafka
    }

    override suspend fun request(topic: String, message: Message, timeout: Long): Message? {
        // TODO: Implement with Kafka
        return null
    }

    override fun reply(replyTo: String, message: Message) {
        // TODO: Implement with Kafka
    }
}

/**
 * Default implementation of MessageBuilder
 */
class DefaultMessageBuilder : MessageBuilder {
    private var payload: ByteArray? = null
    private val headers = mutableMapOf<String, String>()
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

    override fun build(): Message {
        requireNotNull(payload) { "Payload must be set" }
        return DefaultMessage(
            payload = payload!!,
            headers = headers.toMap(),
            correlationId = correlationId,
            replyTo = replyTo
        )
    }
}
