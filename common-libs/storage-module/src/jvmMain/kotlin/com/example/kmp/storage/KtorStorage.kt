package com.example.kmp.storage

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Ktor-specific implementation of StorageProvider
 */
class KtorStorageProvider(private val config: StorageConfig) : StorageProvider {
    // In-memory storage for demonstration
    // In a real implementation, this would use actual databases/caches based on config.type
    private val storage = ConcurrentHashMap<String, Any>()

    override suspend fun <T> read(key: String, type: Class<T>): StorageResult<T> {
        return try {
            val value = storage[key]
            if (value != null && type.isInstance(value)) {
                @Suppress("UNCHECKED_CAST")
                StorageResult.Success(value as T)
            } else {
                StorageResult.Error("Key not found or invalid type", StorageErrorCode.NOT_FOUND)
            }
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Read failed", StorageErrorCode.SERVER_ERROR)
        }
    }

    override suspend fun <T> write(key: String, value: T): StorageResult<Unit> {
        return try {
            storage[key] = value as Any
            StorageResult.Success(Unit)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Write failed", StorageErrorCode.SERVER_ERROR)
        }
    }

    override suspend fun delete(key: String): StorageResult<Unit> {
        return try {
            storage.remove(key)
            StorageResult.Success(Unit)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Delete failed", StorageErrorCode.SERVER_ERROR)
        }
    }

    override suspend fun exists(key: String): Boolean {
        return storage.containsKey(key)
    }

    override suspend fun <T> query(query: StorageQuery<T>): StorageResult<List<T>> {
        return try {
            // In a real implementation, this would translate the query to the appropriate
            // database query language (SQL, MongoDB query, etc.)
            val results = storage.entries
                .filter { entry ->
                    query.filter.all { (key, value) ->
                        when (value) {
                            is String -> entry.key.contains(value) || 
                                       (entry.value.toString().contains(value) && entry.key.contains(key))
                            else -> entry.value == value && entry.key.contains(key)
                        }
                    }
                }
                .map { it.value }
                .let { list ->
                    // Apply sorting
                    var sorted = list
                    query.sort.forEach { (key, order) ->
                        sorted = when (order) {
                            SortOrder.ASCENDING -> sorted.sortedBy { 
                                when (val value = it) {
                                    is Map<*, *> -> value[key]?.toString() ?: ""
                                    else -> value.toString()
                                }
                            }
                            SortOrder.DESCENDING -> sorted.sortedByDescending { 
                                when (val value = it) {
                                    is Map<*, *> -> value[key]?.toString() ?: ""
                                    else -> value.toString()
                                }
                            }
                        }
                    }
                    sorted
                }
                .let { list ->
                    // Apply pagination
                    var paginated = list
                    query.offset?.let { offset ->
                        paginated = paginated.drop(offset)
                    }
                    query.limit?.let { limit ->
                        paginated = paginated.take(limit)
                    }
                    paginated
                }

            @Suppress("UNCHECKED_CAST")
            StorageResult.Success(results as List<T>)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Query failed", StorageErrorCode.SERVER_ERROR)
        }
    }

    override suspend fun transaction(block: suspend StorageTransaction.() -> Unit): StorageResult<Unit> {
        val transaction = KtorStorageTransaction(this)
        return try {
            block(transaction)
            StorageResult.Success(Unit)
        } catch (e: Exception) {
            StorageResult.Error(e.message ?: "Transaction failed", StorageErrorCode.TRANSACTION_FAILED)
        }
    }
}

/**
 * Ktor-specific implementation of StorageTransaction
 */
class KtorStorageTransaction(private val provider: StorageProvider) : StorageTransaction {
    override suspend fun <T> read(key: String, type: Class<T>) = provider.read(key, type)
    override suspend fun <T> write(key: String, value: T) = provider.write(key, value)
    override suspend fun delete(key: String) = provider.delete(key)
    override suspend fun exists(key: String) = provider.exists(key)
}

/**
 * Ktor-specific implementation of QueryBuilder
 */
class KtorQueryBuilder<T> : QueryBuilder<T> {
    private val filters = mutableMapOf<String, Any>()
    private val sorting = mutableMapOf<String, SortOrder>()
    private var limitValue: Int? = null
    private var offsetValue: Int? = null

    override fun filter(key: String, value: Any): QueryBuilder<T> {
        filters[key] = value
        return this
    }

    override fun sort(key: String, order: SortOrder): QueryBuilder<T> {
        sorting[key] = order
        return this
    }

    override fun limit(limit: Int): QueryBuilder<T> {
        limitValue = limit
        return this
    }

    override fun offset(offset: Int): QueryBuilder<T> {
        offsetValue = offset
        return this
    }

    override fun build(): StorageQuery<T> = object : StorageQuery<T> {
        override val filter = filters.toMap()
        override val sort = sorting.toMap()
        override val limit = limitValue
        override val offset = offsetValue
    }
}

/**
 * Ktor-specific implementation of StorageFactory
 */
class KtorStorageFactory : StorageFactory {
    override fun createStorageProvider(config: StorageConfig): StorageProvider = KtorStorageProvider(config)
    override fun <T> createQueryBuilder(): QueryBuilder<T> = KtorQueryBuilder()

    companion object {
        fun initialize() {
            StorageFactory.initialize(KtorStorageFactory())
        }
    }
}

// Initialize the Ktor implementation
private val initializeKtorStorage = KtorStorageFactory.initialize()
