package com.example.kmp.di

import com.example.kmp.auth.AuthConfig
import com.example.kmp.auth.AuthFactory
import com.example.kmp.auth.AuthMethod
import com.example.kmp.auth.KtorAuthFactory
import com.example.kmp.messaging.KtorMessagingFactory
import com.example.kmp.messaging.MessagingConfig
import com.example.kmp.messaging.MessagingFactory
import com.example.kmp.storage.KtorStorageFactory
import com.example.kmp.storage.StorageConfig
import com.example.kmp.storage.StorageFactory
import com.example.kmp.storage.StorageType
import com.example.kmp.validation.ValidationService
import org.koin.dsl.module

/**
 * Common infrastructure module that provides core dependencies for KMP services
 */
object InfrastructureModule {
    /**
     * Initialize all infrastructure factories
     */
    fun initialize() {
        KtorStorageFactory.initialize()
        KtorAuthFactory.initialize()
        KtorMessagingFactory.initialize()
    }

    /**
     * Creates a Koin module with standard infrastructure dependencies
     */
    fun create(
        serviceName: String,
        config: InfrastructureConfig = InfrastructureConfig()
    ) = module {
        // Core validation service
        single { ValidationService() }

        // Storage provider if enabled
        if (config.storage.enabled) {
            single {
                StorageFactory.createStorageProvider(StorageConfig(
                    type = config.storage.type,
                    connectionString = config.storage.connectionString
                ))
            }
        }

        // Message broker if enabled
        if (config.messaging.enabled) {
            single {
                MessagingFactory.createMessageBroker(MessagingConfig(
                    brokerUrl = config.messaging.brokerUrl,
                    clientId = serviceName
                ))
            }
        }

        // Auth provider if enabled
        if (config.auth.enabled) {
            single {
                AuthFactory.createAuthProvider(AuthConfig(
                    authServerUrl = config.auth.serverUrl,
                    clientId = serviceName,
                    clientSecret = config.auth.clientSecret,
                    authMethods = config.auth.methods
                ))
            }
        }
    }
}

/**
 * Configuration classes for infrastructure setup
 */
data class InfrastructureConfig(
    val storage: StorageConfig = StorageConfig(),
    val messaging: MessagingConfig = MessagingConfig(),
    val auth: AuthConfig = AuthConfig()
) {
    data class StorageConfig(
        val enabled: Boolean = true,
        val type: StorageType = StorageType.DATABASE,
        val connectionString: String = "mongodb://localhost:27017"
    )

    data class MessagingConfig(
        val enabled: Boolean = true,
        val brokerUrl: String = "localhost:9092"
    )

    data class AuthConfig(
        val enabled: Boolean = true,
        val serverUrl: String = "http://localhost:8081",
        val clientSecret: String = "dev-secret",
        val methods: Set<AuthMethod> = setOf(AuthMethod.JWT)
    )
} 