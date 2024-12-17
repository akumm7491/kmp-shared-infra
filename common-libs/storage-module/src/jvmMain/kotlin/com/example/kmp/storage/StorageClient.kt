package com.example.kmp.storage

actual class StorageClient {
    actual suspend fun <T> getRepository(name: String): Repository<T, String> {
        return object : Repository<T, String> {
            private val storage = mutableMapOf<String, T>()

            override suspend fun findById(id: String): T? = storage[id]

            override suspend fun save(entity: T): T {
                val id = System.currentTimeMillis().toString()
                storage[id] = entity
                return entity
            }

            override suspend fun delete(id: String) {
                storage.remove(id)
            }

            override suspend fun findAll(): List<T> = storage.values.toList()
        }
    }
}
