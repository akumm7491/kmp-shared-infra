package com.example.kmp.storage

interface Repository<T, ID> {
    suspend fun findById(id: ID): T?
    suspend fun save(entity: T): T
    suspend fun delete(id: ID)
    suspend fun findAll(): List<T>
}

expect class StorageClient {
    suspend fun <T> getRepository(name: String): Repository<T, String>
}
