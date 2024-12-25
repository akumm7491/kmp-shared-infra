package com.example.kmp.messaging

/**
 * Generic message broker interface that can be implemented for any platform
 */
interface MessageBroker {
    fun publish(topic: String, message: Message)
    fun subscribe(topic: String, handler: MessageHandler)
    fun unsubscribe(topic: String)
    suspend fun request(topic: String, message: Message, timeout: Long = 5000): Message?
    fun reply(replyTo: String, message: Message)
}

/**
 * Factory interface for creating messaging components
 * Each platform can provide its own implementation
 */
interface MessagingFactory {
    fun createMessageBroker(config: MessagingConfig): MessageBroker
    fun createMessageBuilder(): MessageBuilder

    companion object : MessagingFactory {
        private var instance: MessagingFactory? = null

        fun initialize(factory: MessagingFactory) {
            instance = factory
        }

        override fun createMessageBroker(config: MessagingConfig): MessageBroker {
            return instance?.createMessageBroker(config)
                ?: throw IllegalStateException("MessagingFactory not initialized")
        }

        override fun createMessageBuilder(): MessageBuilder {
            return instance?.createMessageBuilder()
                ?: throw IllegalStateException("MessagingFactory not initialized")
        }
    }
}

/**
 * Generic configuration for message brokers
 */
data class MessagingConfig(
    val brokerUrl: String,
    val clientId: String,
    val username: String? = null,
    val password: String? = null,
    val ssl: Boolean = false,
    val retryPolicy: RetryPolicy = RetryPolicy(),
    val qos: QualityOfService = QualityOfService.AT_LEAST_ONCE
)

/**
 * Generic message interface
 */
interface Message {
    val id: String
    val payload: ByteArray
    val headers: Map<String, String>
    val timestamp: Long
    val correlationId: String?
    val replyTo: String?
}

/**
 * Message handler function type
 */
typealias MessageHandler = suspend (Message) -> Unit

/**
 * Quality of service levels
 */
enum class QualityOfService {
    AT_MOST_ONCE,    // Fire and forget
    AT_LEAST_ONCE,   // Guaranteed delivery
    EXACTLY_ONCE     // Exactly once delivery
}

/**
 * Retry policy configuration
 */
data class RetryPolicy(
    val maxRetries: Int = 3,
    val initialDelay: Long = 1000,
    val maxDelay: Long = 10000,
    val backoffMultiplier: Double = 2.0
)

/**
 * Result of messaging operations
 */
sealed class MessagingResult {
    data class Success(
        val messageId: String
    ) : MessagingResult()

    data class Error(
        val message: String,
        val code: MessagingErrorCode = MessagingErrorCode.UNKNOWN
    ) : MessagingResult()
}

/**
 * Error codes for messaging failures
 */
enum class MessagingErrorCode {
    CONNECTION_ERROR,
    TIMEOUT,
    INVALID_MESSAGE,
    BROKER_UNAVAILABLE,
    AUTHENTICATION_ERROR,
    SERVER_ERROR,
    UNKNOWN
}

/**
 * Message builder for creating messages
 */
interface MessageBuilder {
    fun setPayload(payload: ByteArray): MessageBuilder
    fun addHeader(key: String, value: String): MessageBuilder
    fun setCorrelationId(correlationId: String): MessageBuilder
    fun setReplyTo(replyTo: String): MessageBuilder
    fun build(): Message
}
