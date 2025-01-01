package com.example.kmp.template.di

import com.example.kmp.auth.AuthFactory
import com.example.kmp.auth.AuthConfig
import com.example.kmp.auth.AuthMethod
import com.example.kmp.messaging.MessagingFactory
import com.example.kmp.messaging.MessagingConfig
import com.example.kmp.storage.StorageFactory
import com.example.kmp.storage.StorageConfig
import com.example.kmp.storage.StorageType
import com.example.kmp.template.config.AppConfig
import com.example.kmp.template.service.DemoService
import com.example.kmp.validation.ValidationService
import org.koin.dsl.module

/**
 * Koin module for service dependencies
 */
val serviceModule = module {
    // Core services
    single { 
        StorageFactory.createStorageProvider(StorageConfig(
            type = StorageType.DATABASE,
            connectionString = "localhost:27017"
        ))
    }
    
    single {
        MessagingFactory.createMessageBroker(MessagingConfig(
            brokerUrl = "localhost:9092",
            clientId = AppConfig.SERVICE_NAME
        ))
    }
    
    single {
        AuthFactory.createAuthProvider(AuthConfig(
            authServerUrl = "http://localhost:8081",
            clientId = AppConfig.SERVICE_NAME,
            clientSecret = "secret",
            authMethods = setOf(AuthMethod.JWT)
        ))
    }
    
    single { ValidationService() }
    
    // Application services
    single { DemoService(get(), get(), get(), get()) }
} 