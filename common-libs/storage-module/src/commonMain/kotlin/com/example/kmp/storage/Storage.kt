package com.example.kmp.storage

/**
 * Generic storage provider interface that can be implemented for any platform
 */
interface StorageProvider {
    suspend fun <T> read(key: String, type: Class<T>): StorageResult<T>
    suspend fun <T> write(key: String, value: T): StorageResult<Unit>
    suspend fun delete(key: String): StorageResult<Unit>
    suspend fun exists(key: String): Boolean
    suspend fun <T> query(query: StorageQuery<T>): StorageResult<List<T>>
    suspend fun transaction(block: suspend StorageTransaction.() -> Unit): StorageResult<Unit>
}

/**
 * Factory interface for creating storage components
 * Each platform can provide its own implementation
 */
interface StorageFactory {
    fun createStorageProvider(config: StorageConfig): StorageProvider
    fun <T> createQueryBuilder(): QueryBuilder<T>

    companion object : StorageFactory {
        private var instance: StorageFactory? = null

        fun initialize(factory: StorageFactory) {
            instance = factory
        }

        override fun createStorageProvider(config: StorageConfig): StorageProvider {
            return instance?.createStorageProvider(config)
                ?: throw IllegalStateException("StorageFactory not initialized")
        }

        override fun <T> createQueryBuilder(): QueryBuilder<T> {
            return instance?.createQueryBuilder()
                ?: throw IllegalStateException("StorageFactory not initialized")
        }
    }
}

/**
 * Generic configuration for storage providers
 */
data class StorageConfig(
    val type: StorageType,
    val connectionString: String,
    val credentials: StorageCredentials? = null,
    val options: Map<String, String> = emptyMap(),
    val retryPolicy: RetryPolicy = RetryPolicy()
)

/**
 * Types of storage
 */
enum class StorageType {
    DATABASE,
    CACHE,
    FILE_SYSTEM,
    OBJECT_STORE
}

/**
 * Storage credentials
 */
data class StorageCredentials(
    val username: String? = null,
    val password: String? = null,
    val accessKey: String? = null,
    val secretKey: String? = null
)

/**
 * Query interface for storage operations
 */
interface StorageQuery<T> {
    val filter: Map<String, Any>
    val sort: Map<String, SortOrder>
    val limit: Int?
    val offset: Int?
}

/**
 * Sort order for queries
 */
enum class SortOrder {
    ASCENDING,
    DESCENDING
}

/**
 * Transaction interface for atomic operations
 */
interface StorageTransaction {
    suspend fun <T> read(key: String, type: Class<T>): StorageResult<T>
    suspend fun <T> write(key: String, value: T): StorageResult<Unit>
    suspend fun delete(key: String): StorageResult<Unit>
    suspend fun exists(key: String): Boolean
}

/**
 * Result of storage operations
 */
sealed class StorageResult<T> {
    data class Success<T>(
        val data: T
    ) : StorageResult<T>()

    data class Error<T>(
        val message: String,
        val code: StorageErrorCode = StorageErrorCode.UNKNOWN
    ) : StorageResult<T>()
}

/**
 * Error codes for storage failures
 */
enum class StorageErrorCode {
    CONNECTION_ERROR,
    NOT_FOUND,
    ALREADY_EXISTS,
    INVALID_DATA,
    PERMISSION_DENIED,
    TRANSACTION_FAILED,
    SERVER_ERROR,
    UNKNOWN
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
 * Query builder for creating storage queries
 */
interface QueryBuilder<T> {
    fun filter(key: String, value: Any): QueryBuilder<T>
    fun sort(key: String, order: SortOrder = SortOrder.ASCENDING): QueryBuilder<T>
    fun limit(limit: Int): QueryBuilder<T>
    fun offset(offset: Int): QueryBuilder<T>
    fun build(): StorageQuery<T>
}
