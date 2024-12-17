package com.example.kmp.auth

actual class TokenValidator {
    actual suspend fun validateToken(token: String): TokenValidationResult {
        // Stub implementation
        return if (token.isNotBlank() && token != "invalid") {
            TokenValidationResult(
                isValid = true,
                userId = "user-123"
            )
        } else {
            TokenValidationResult(
                isValid = false,
                error = "Invalid token"
            )
        }
    }

    actual fun isTokenExpired(token: String): Boolean {
        // Stub implementation
        return token == "expired"
    }
}
