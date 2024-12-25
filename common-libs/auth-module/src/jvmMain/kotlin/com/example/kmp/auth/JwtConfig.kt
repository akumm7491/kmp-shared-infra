package com.example.kmp.auth

/**
 * Configuration for JWT authentication
 */
data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String
)

/**
 * Generate a JWT token with the given configuration and claims
 */
fun generateToken(
    config: JwtConfig,
    subject: String,
    claims: Map<String, Any> = emptyMap()
): String {
    // TODO: Implement actual JWT token generation using a JWT library
    // For now, return a mock token for testing
    return "mock.jwt.token"
}
