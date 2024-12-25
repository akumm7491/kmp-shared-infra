package com.example.kmp.auth.test

import com.example.kmp.auth.JwtConfig
import com.example.kmp.auth.generateToken
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Test utilities for JWT authentication
 */
object AuthTestUtils {
    /**
     * Default test JWT configuration
     */
    val defaultTestConfig = JwtConfig(
        secret = "test-secret-key",
        issuer = "http://localhost:8080",
        audience = "http://localhost:8080",
        realm = "Test Realm"
    )

    /**
     * Generate a test JWT token with custom configuration
     */
    fun generateTestToken(
        config: JwtConfig = defaultTestConfig,
        subject: String = "test-user",
        roles: List<String> = listOf("admin"),
        permissions: List<String> = listOf("service.manage"),
        additionalClaims: Map<String, Any> = emptyMap()
    ): String {
        return generateToken(
            config = config,
            subject = subject,
            claims = buildMap {
                put("roles", roles)
                put("permissions", permissions)
                putAll(additionalClaims)
            }
        )
    }

    /**
     * Extension function to add auth header to requests
     */
    fun HttpRequestBuilder.withTestAuth(
        config: JwtConfig = defaultTestConfig,
        roles: List<String> = listOf("admin"),
        permissions: List<String> = listOf("service.manage")
    ) {
        header(HttpHeaders.Authorization, "Bearer ${generateTestToken(config, roles = roles, permissions = permissions)}")
    }

    /**
     * Create test JWT configuration for a specific service
     */
    fun createServiceTestConfig(
        serviceName: String,
        port: Int = 8080
    ): JwtConfig {
        return JwtConfig(
            secret = "test-secret-key",
            issuer = "http://localhost:$port",
            audience = "http://localhost:$port",
            realm = "$serviceName Test"
        )
    }
}
