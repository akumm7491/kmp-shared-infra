package com.example.kmp.auth

/**
 * Generic authentication provider interface that can be implemented for any platform
 */
interface AuthProvider {
    fun authenticate(credentials: AuthCredentials): AuthResult
    fun authorize(token: String, requiredPermissions: Set<String>): AuthResult
    fun validateToken(token: String): Boolean
    fun refreshToken(token: String): AuthResult
}

/**
 * Factory interface for creating auth components
 * Each platform can provide its own implementation
 */
interface AuthFactory {
    fun createAuthProvider(config: AuthConfig): AuthProvider

    companion object : AuthFactory {
        private var instance: AuthFactory? = null

        fun initialize(factory: AuthFactory) {
            instance = factory
        }

        override fun createAuthProvider(config: AuthConfig): AuthProvider {
            return instance?.createAuthProvider(config)
                ?: throw IllegalStateException("AuthFactory not initialized")
        }
    }
}

/**
 * Generic configuration for auth providers
 */
data class AuthConfig(
    val authServerUrl: String,
    val clientId: String,
    val clientSecret: String,
    val tokenExpiration: Long = 3600,
    val refreshTokenExpiration: Long = 86400,
    val authMethods: Set<AuthMethod> = setOf(AuthMethod.JWT)
)

/**
 * Supported authentication methods
 */
enum class AuthMethod {
    JWT,
    OAUTH2,
    SESSION,
    API_KEY
}

/**
 * Generic credentials interface
 */
interface AuthCredentials

/**
 * Username/password credentials implementation
 */
data class UserPasswordCredentials(
    val username: String,
    val password: String
) : AuthCredentials

/**
 * API key credentials implementation
 */
data class ApiKeyCredentials(
    val apiKey: String
) : AuthCredentials

/**
 * OAuth2 credentials implementation
 */
data class OAuth2Credentials(
    val code: String,
    val redirectUri: String
) : AuthCredentials

/**
 * Result of authentication/authorization operations
 */
sealed class AuthResult {
    data class Success(
        val token: String,
        val refreshToken: String? = null,
        val expiresIn: Long? = null,
        val permissions: Set<String> = emptySet()
    ) : AuthResult()

    data class Error(
        val message: String,
        val code: AuthErrorCode = AuthErrorCode.UNKNOWN
    ) : AuthResult()
}

/**
 * Error codes for authentication failures
 */
enum class AuthErrorCode {
    INVALID_CREDENTIALS,
    EXPIRED_TOKEN,
    INVALID_TOKEN,
    INSUFFICIENT_PERMISSIONS,
    SERVER_ERROR,
    UNKNOWN
}
