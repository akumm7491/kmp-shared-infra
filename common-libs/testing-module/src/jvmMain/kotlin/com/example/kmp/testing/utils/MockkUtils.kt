package com.example.kmp.testing.utils

import io.mockk.*
import io.mockk.impl.annotations.MockK

/**
 * Utility functions for Mockk testing
 */
object MockkUtils {
    /**
     * Creates a mock instance of type T
     */
    inline fun <reified T : Any> mock(relaxed: Boolean = false): T = mockk(relaxed = relaxed)

    /**
     * Creates a relaxed mock instance of type T
     */
    inline fun <reified T : Any> relaxedMock(): T = mockk(relaxed = true)

    /**
     * Extension functions for MockKMatcherScope
     */
    fun MockKMatcherScope.anyString(): String = any()
    fun MockKMatcherScope.anyInt(): Int = any()
    fun MockKMatcherScope.anyLong(): Long = any()
    fun MockKMatcherScope.anyDouble(): Double = any()
    fun MockKMatcherScope.anyBoolean(): Boolean = any()
    
    /**
     * Type-safe list matcher
     */
    inline fun <reified T : Any> MockKMatcherScope.anyList(): List<T> = any()

    /**
     * Type-safe map matcher
     */
    inline fun <reified K : Any, reified V : Any> MockKMatcherScope.anyMap(): Map<K, V> = any()

    /**
     * Matches an exact value
     */
    inline fun <reified T : Any> MockKMatcherScope.eqValue(value: T): T = eq(value)

    /**
     * Captures a value into a slot
     */
    inline fun <reified T : Any> MockKMatcherScope.captureValue(slot: CapturingSlot<T>): T = capture(slot)

    /**
     * Matches a value using a predicate
     */
    inline fun <reified T : Any> MockKMatcherScope.matchValue(noinline predicate: (T) -> Boolean): T = match(predicate)

    /**
     * Sets up a mock to return a value
     */
    fun <T> MockKStubScope<T, T>.returnsValue(value: T): MockKAdditionalAnswerScope<T, T> = returns(value)

    /**
     * Verifies that a mock was called
     */
    fun verifyOnce(verifyBlock: MockKVerificationScope.() -> Unit) = verify(exactly = 1, verifyBlock = verifyBlock)

    /**
     * Verifies that a mock was never called
     */
    fun verifyNever(verifyBlock: MockKVerificationScope.() -> Unit) = verify(exactly = 0, verifyBlock = verifyBlock)

    /**
     * Verifies that a mock was called exactly N times
     */
    fun verifyExactly(times: Int, verifyBlock: MockKVerificationScope.() -> Unit) = verify(exactly = times, verifyBlock = verifyBlock)

    /**
     * Creates a capturing slot for a type T
     */
    inline fun <reified T : Any> createSlot(): CapturingSlot<T> = slot()

    /**
     * Creates a list of capturing slots for a type T
     */
    fun <T> createMutableList(): MutableList<T> = mutableListOf()

    /**
     * Clears all mocks
     */
    fun clearAllMocks(vararg mocks: Any) {
        if (mocks.isEmpty()) {
            clearMocks(mockk<Any>())
        } else {
            mocks.forEach { clearMocks(it) }
        }
    }

    /**
     * Unmocks all mocks
     */
    fun clearAndUnmockAll() {
        clearMocks(mockk<Any>())
        io.mockk.unmockkAll()
    }
}
