package com.example.kmp.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokenValidationResult(
    val isValid: Boolean,
    val userId: String? = null,
    val error: String? = null
)

expect class TokenValidator {
    suspend fun validateToken(token: String): TokenValidationResult
    
    fun isTokenExpired(token: String): Boolean
}
