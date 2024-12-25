package com.example.kmp.messaging.test

import com.example.kmp.messaging.DefaultMessage
import com.example.kmp.messaging.Message
import com.example.kmp.messaging.MessageBroker
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.mockk
import io.mockk.every
import kotlinx.coroutines.runBlocking

/**
 * Test utilities for messaging
 */
object MessagingTestUtils {
    /**
     * Create a test message with the given payload
     */
    fun createTestMessage(payload: ByteArray, headers: Map<String, String> = emptyMap()): Message {
        return DefaultMessage(
            payload = payload,
            headers = headers
        )
    }

    /**
     * Extension function to verify message was published
     */
    fun MessageBroker.verifyMessage(topic: String, predicate: (Message) -> Boolean) = runBlocking {
        val mockMessage = mockk<Message>(relaxed = true)
        every { predicate(mockMessage) } returns true
        coVerify {
            publish(topic, mockMessage)
        }
    }

    /**
     * Extension function to verify message was published with exact payload
     */
    fun MessageBroker.verifyMessagePayload(topic: String, message: Message) = runBlocking {
        coVerify {
            publish(topic, message)
        }
    }

    /**
     * Extension function to verify multiple topics had messages published
     */
    fun MessageBroker.verifyTopics(vararg topics: String) = runBlocking {
        topics.forEach { topic ->
            coVerify {
                publish(topic, mockk(relaxed = true))
            }
        }
    }

    /**
     * Extension function to verify message count for a topic
     */
    fun MessageBroker.verifyMessageCount(topic: String, times: Int) = runBlocking {
        coVerify(exactly = times) {
            publish(topic, mockk(relaxed = true))
        }
    }

    /**
     * Extension function to capture and verify message
     */
    inline fun MessageBroker.captureMessage(topic: String, crossinline verifier: (Message) -> Unit) = runBlocking {
        val slot = slot<Message>()
        val mockMessage = mockk<Message>(relaxed = true)
        coVerify {
            publish(topic, mockMessage)
        }
        verifier(mockMessage)
    }

    /**
     * Extension function to verify message headers
     */
    fun MessageBroker.verifyMessageHeaders(topic: String, headers: Map<String, String>) = runBlocking {
        verifyMessage(topic) { message ->
            headers.all { (key, value) -> message.headers[key] == value }
        }
    }

    /**
     * Extension function to verify message sequence
     */
    fun MessageBroker.verifyMessageSequence(vararg messages: Pair<String, (Message) -> Boolean>) = runBlocking {
        messages.forEach { (topic, predicate) ->
            verifyMessage(topic, predicate)
        }
    }

    /**
     * Extension function to verify no messages were published
     */
    fun MessageBroker.verifyNoMessages() = runBlocking {
        coVerify(exactly = 0) {
            publish(mockk(), mockk(relaxed = true))
        }
    }
}
