# Authentication Module

The Authentication module provides a comprehensive solution for service authentication, authorization, and user management.

## Features

- OAuth 2.0 / OpenID Connect
- JWT token handling
- Role-based access control
- User session management
- Multi-factor authentication
- API key authentication

## Integration

### 1. Gradle Setup

```kotlin
// build.gradle.kts
plugins {
    id("com.example.kmp.auth")
}

kmpAuth {
    providers {
        oauth {
            enabled = true
            issuer = "https://auth.example.com"
        }
        jwt {
            enabled = true
            issuer = "my-service"
        }
    }
    
    security {
        cors = true
        csrf = true
        rateLimit = true
    }
}
```

### 2. Service Configuration

```kotlin
@Configuration
class AuthConfig {
    @Bean
    fun authProvider(): AuthProvider {
        return AuthProvider {
            // OAuth configuration
            oauth {
                clientId = config.getString("auth.client.id")
                clientSecret = config.getString("auth.client.secret")
                scopes = listOf("openid", "profile", "email")
            }
            
            // JWT configuration
            jwt {
                algorithm = Algorithm.RS256
                publicKey = loadPublicKey()
                privateKey = loadPrivateKey()
            }
            
            // Session configuration
            session {
                duration = 24.hours
                refreshToken = true
            }
        }
    }
}
```

## Usage

### 1. Authentication

```kotlin
@Controller
class AuthController(
    private val auth: AuthProvider
) {
    @Post("/login")
    suspend fun login(
        @Body credentials: Credentials
    ): AuthResponse {
        return auth.authenticate(credentials)
    }
    
    @Post("/logout")
    @Authenticated
    suspend fun logout(
        @AuthContext context: AuthContext
    ) {
        auth.logout(context.sessionId)
    }
    
    @Post("/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequest
    ): AuthResponse {
        return auth.refresh(request.token)
    }
}
```

### 2. Authorization

```kotlin
@Service
class UserService {
    @Authenticated
    @HasRole("user.write")
    suspend fun createUser(
        @AuthContext context: AuthContext,
        request: CreateUserRequest
    ): User {
        require(context.hasPermission("user.create")) {
            "Insufficient permissions"
        }
        
        return userRepository.create(request)
    }
    
    @Authenticated
    @HasAnyRole("user.read", "admin")
    suspend fun getUser(
        userId: String
    ): User {
        return userRepository.findById(userId)
    }
}
```

### 3. API Key Authentication

```kotlin
@Configuration
class ApiKeyConfig {
    @Bean
    fun apiKeyProvider(): ApiKeyProvider {
        return ApiKeyProvider {
            header = "X-API-Key"
            validator = { key ->
                apiKeyRepository.validate(key)
            }
        }
    }
}

@Controller
class ApiController {
    @Get("/api/data")
    @ApiKeyAuth
    suspend fun getData(
        @ApiKey key: ApiKeyContext
    ): Data {
        return dataService.getData(key.scope)
    }
}
```

## Security Features

### 1. CORS Configuration

```kotlin
@Configuration
class SecurityConfig {
    fun configureCors() {
        security.cors {
            allowOrigins = listOf(
                "https://app.example.com"
            )
            allowMethods = listOf(
                HttpMethod.GET,
                HttpMethod.POST
            )
            allowHeaders = listOf(
                "Authorization",
                "Content-Type"
            )
            exposeHeaders = listOf(
                "X-Auth-Token"
            )
            maxAge = 3600
        }
    }
}
```

### 2. Rate Limiting

```kotlin
@Configuration
class RateLimitConfig {
    fun configureRateLimit() {
        security.rateLimit {
            default {
                limit = 100
                window = 1.minutes
            }
            
            path("/api/public/**") {
                limit = 10
                window = 1.minutes
            }
            
            path("/api/auth/**") {
                limit = 5
                window = 1.minutes
            }
        }
    }
}
```

### 3. Session Management

```kotlin
@Configuration
class SessionConfig {
    fun configureSession() {
        security.session {
            cookie {
                name = "session-id"
                secure = true
                httpOnly = true
                sameSite = SameSite.LAX
            }
            
            storage {
                redis {
                    prefix = "session:"
                    ttl = 24.hours
                }
            }
        }
    }
}
```

## Customization

### 1. Custom Authentication Provider

```kotlin
class CustomAuthProvider : AuthProvider {
    override suspend fun authenticate(
        credentials: Credentials
    ): AuthResult {
        // Custom authentication logic
        return when (validateCredentials(credentials)) {
            true -> AuthResult.Success(createToken())
            false -> AuthResult.Failure("Invalid credentials")
        }
    }
    
    override suspend fun validate(
        token: String
    ): ValidationResult {
        // Custom token validation
        return try {
            val claims = validateToken(token)
            ValidationResult.Valid(claims)
        } catch (e: Exception) {
            ValidationResult.Invalid(e.message)
        }
    }
}
```

### 2. Custom Authorization Rules

```kotlin
@Configuration
class AuthorizationConfig {
    fun configureRules() {
        security.authorization {
            rule("admin-only") {
                hasRole("admin")
                hasScope("full-access")
            }
            
            rule("read-only") {
                hasAnyRole("reader", "viewer")
                hasScope("read")
            }
            
            rule("api-access") {
                hasValidApiKey()
                hasScope("api")
            }
        }
    }
}
```

## Monitoring

### 1. Security Events

```kotlin
@Configuration
class SecurityMonitoring {
    fun configureEvents() {
        security.events {
            onLogin { event ->
                metrics.counter("auth.login.attempts")
                    .tag("success", event.success)
                    .increment()
            }
            
            onFailure { event ->
                logger.warn("Authentication failure", {
                    "reason" to event.reason,
                    "ip" to event.ipAddress
                })
            }
            
            onTokenRevoked { event ->
                logger.info("Token revoked", {
                    "userId" to event.userId,
                    "reason" to event.reason
                })
            }
        }
    }
}
```

### 2. Metrics

```kotlin
@MetricsConfiguration
class AuthMetrics {
    fun registerMetrics() {
        metrics.counter("auth.requests") {
            tags("type", "status")
        }
        
        metrics.gauge("auth.active_sessions") {
            sessionManager.activeSessions()
        }
        
        metrics.timer("auth.token_validation") {
            tags("type")
        }
    }
}
```

## Best Practices

1. **Token Management**
   - Use short-lived access tokens
   - Implement token refresh mechanism
   - Store tokens securely
   - Implement token revocation

2. **Security Headers**
   - Enable HSTS
   - Set secure cookie attributes
   - Configure CSP
   - Enable XSS protection

3. **Error Handling**
   - Don't leak sensitive information
   - Log security events
   - Implement rate limiting
   - Use secure error messages

## References

1. [OAuth 2.0 Specification](https://oauth.net/2/)
2. [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)
3. [OWASP Authentication Cheatsheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
