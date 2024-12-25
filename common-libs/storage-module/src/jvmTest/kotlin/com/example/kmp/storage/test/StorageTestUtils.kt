package com.example.kmp.storage.test

import com.example.kmp.storage.StorageProvider
import com.example.kmp.storage.StorageResult
import com.example.kmp.storage.StorageQuery
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

/**
 * Test utilities for storage and caching
 */
object StorageTestUtils {
    /**
     * Extension function to verify read operations
     */
    inline fun <reified T : Any> StorageProvider.verifyRead(key: String, times: Int = 1) = runBlocking {
        coVerify(exactly = times) {
            read(key, T::class.java)
        }
    }

    /**
     * Extension function to verify write operation
     */
    inline fun <reified T : Any> StorageProvider.verifyWrite(key: String, value: T, times: Int = 1) = runBlocking {
        coVerify(exactly = times) {
            write(key, value)
        }
    }

    /**
     * Extension function to verify delete operation
     */
    fun StorageProvider.verifyDelete(key: String, times: Int = 1) = runBlocking {
        coVerify(exactly = times) {
            delete(key)
        }
    }

    /**
     * Extension function to verify operations sequence
     */
    inline fun <reified T : Any> StorageProvider.verifySequence(vararg operations: Pair<String, String>) = runBlocking {
        operations.forEach { (operation, key) ->
            when (operation) {
                "read" -> verifyRead<T>(key)
                "write" -> verifyWrite(key, mockk<T>())
                "delete" -> verifyDelete(key)
            }
        }
    }

    /**
     * Extension function to capture and verify stored value
     */
    inline fun <reified T : Any> StorageProvider.verifyValue(key: String, crossinline verifier: (T) -> Unit) = runBlocking {
        val slot = slot<T>()
        coVerify {
            write(key, capture(slot))
        }
        verifier(slot.captured)
    }

    /**
     * Extension function to verify cache miss
     */
    inline fun <reified T : Any> StorageProvider.verifyMiss(key: String) = runBlocking {
        coVerify {
            read(key, T::class.java)
        }
        coVerify(exactly = 0) {
            write(key, mockk<T>())
        }
    }

    /**
     * Extension function to verify cache hit
     */
    inline fun <reified T : Any> StorageProvider.verifyHit(key: String) = runBlocking {
        coVerify {
            read(key, T::class.java)
        }
        coVerify(exactly = 0) {
            write(key, mockk<T>())
        }
    }

    /**
     * Extension function to verify cache invalidation
     */
    inline fun <reified T : Any> StorageProvider.verifyInvalidation(key: String) = runBlocking {
        coVerify {
            delete(key)
        }
        coVerify {
            read(key, T::class.java)
        }
        coVerify {
            write(key, mockk<T>())
        }
    }

    /**
     * Extension function to verify query operation
     */
    fun <T : Any> StorageProvider.verifyQuery(query: StorageQuery<T>) = runBlocking {
        coVerify {
            query(query)
        }
    }
}
