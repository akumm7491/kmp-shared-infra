package com.example.kmp.messaging

import kotlinx.coroutines.withTimeout
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Ktor-specific implementation of Message
 */
data class KtorMessage(
    override val id: String = UUID.randomUUID().toString(),
    override val payload: ByteArray,
    override val headers: Map<String, String> = emptyMap(),
    override val timestamp: Long = System.currentTimeMillis(),
    override val correlationId: String? = null,
    override val replyTo: String? = null
) : Message {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KtorMessage) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Ktor-specific implementation of MessageBroker
 */
class KtorMessageBroker(private val config: MessagingConfig) : MessageBroker {
    private val subscribers = ConcurrentHashMap<String, MutableList<MessageHandler>>()
    private val replyHandlers = ConcurrentHashMap<String, suspend (Message) -> Unit>()

    override fun publish(topic: String, message: Message) {
        subscribers[topic]?.forEach { handler ->
            // In a real implementation, this would be handled by a proper message broker
            kotlinx.coroutines.runBlocking {
                try {
                    handler(message)
                } catch (e: Exception) {
                    // Log error and potentially retry based on config.retryPolicy
                }
            }
        }
    }

    override fun subscribe(topic: String, handler: MessageHandler) {
        subscribers.computeIfAbsent(topic) { mutableListOf() }.add(handler)
    }

    override fun unsubscribe(topic: String) {
        subscribers.remove(topic)
    }

    override suspend fun request(topic: String, message: Message, timeout: Long): Message? {
        val replyTo = UUID.randomUUID().toString()
        var response: Message? = null

        try {
            withTimeout(timeout) {
                val latch = kotlinx.coroutines.CompletableDeferred<Message>()
                
                replyHandlers[replyTo] = { reply ->
                    latch.complete(reply)
                }

                val requestMessage = KtorMessage(
                    payload = message.payload,
                    headers = message.headers,
                    correlationId = message.id,
                    replyTo = replyTo
                )

                publish(topic, requestMessage)
                response = latch.await()
            }
        } catch (e: Exception) {
            // Handle timeout or other errors
        } finally {
            replyHandlers.remove(replyTo)
        }

        return response
    }

    override fun reply(replyTo: String, message: Message) {
        replyHandlers[replyTo]?.let { handler ->
            kotlinx.coroutines.runBlocking {
                handler(message)
            }
        }
    }
}

/**
 * Ktor-specific implementation of MessageBuilder
 */
class KtorMessageBuilder : MessageBuilder {
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
        requireNotNull(payload) { "Payload is required" }
        return KtorMessage(
            payload = payload!!,
            headers = headers.toMap(),
            correlationId = correlationId,
            replyTo = replyTo
        )
    }
}

/**
 * Ktor-specific implementation of MessagingFactory
 */
class KtorMessagingFactory : MessagingFactory {
    override fun createMessageBroker(config: MessagingConfig): MessageBroker = KtorMessageBroker(config)
    override fun createMessageBuilder(): MessageBuilder = KtorMessageBuilder()

    companion object {
        fun initialize() {
            MessagingFactory.initialize(KtorMessagingFactory())
        }
    }
}

// Initialize the Ktor implementation
private val initializeKtorMessaging = KtorMessagingFactory.initialize()
