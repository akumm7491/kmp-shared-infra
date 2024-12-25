package com.example.kmp.storage

/**
 * Local implementation of StorageProvider that uses an in-memory cache
 */
class LocalStorageProvider : StorageProvider {
    private val cache = mutableMapOf<String, Any>()

    override suspend fun <T> read(key: String, type: Class<T>): StorageResult<T> {
        return try {
            val value = cache[key]
            if (value != null && type.isInstance(value)) {
                @Suppress("UNCHECKED_CAST")
                StorageResult.Success(value as T)
            } else {
                StorageResult.Error("Value not found or wrong type", StorageErrorCode.NOT_FOUND)
            }
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Unknown error", StorageErrorCode.UNKNOWN)
        }
    }

    override suspend fun <T> write(key: String, value: T): StorageResult<Unit> {
        return try {
            cache[key] = value as Any
            StorageResult.Success(Unit)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Unknown error", StorageErrorCode.UNKNOWN)
        }
    }

    override suspend fun delete(key: String): StorageResult<Unit> {
        return try {
            cache.remove(key)
            StorageResult.Success(Unit)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Unknown error", StorageErrorCode.UNKNOWN)
        }
    }

    override suspend fun exists(key: String): Boolean {
        return cache.containsKey(key)
    }

    override suspend fun <T> query(query: StorageQuery<T>): StorageResult<List<T>> {
        // Simple implementation that returns empty list since this is just a cache
        return StorageResult.Success(emptyList())
    }

    override suspend fun transaction(block: suspend StorageTransaction.() -> Unit): StorageResult<Unit> {
        // Simple implementation that executes the block directly since this is just a cache
        return try {
            val transaction = object : StorageTransaction {
                override suspend fun <T> read(key: String, type: Class<T>) = this@LocalStorageProvider.read(key, type)
                override suspend fun <T> write(key: String, value: T) = this@LocalStorageProvider.write(key, value)
                override suspend fun delete(key: String) = this@LocalStorageProvider.delete(key)
                override suspend fun exists(key: String) = this@LocalStorageProvider.exists(key)
            }
            block(transaction)
            StorageResult.Success(Unit)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Transaction failed", StorageErrorCode.TRANSACTION_FAILED)
        }
    }
}
