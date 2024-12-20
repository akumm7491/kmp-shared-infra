package com.example.kmp.networking.models

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val data: T? = null,
    val error: ApiError? = null,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, String> = emptyMap()
)

@Serializable
data class HealthStatus(
    val status: String,
    val timestamp: Long,
    val components: Map<String, String>
)
