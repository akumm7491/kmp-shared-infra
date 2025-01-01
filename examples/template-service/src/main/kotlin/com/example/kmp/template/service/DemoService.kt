package com.example.kmp.template.service

import com.example.kmp.auth.AuthProvider
import com.example.kmp.auth.AuthResult
import com.example.kmp.messaging.MessageBroker
import com.example.kmp.storage.StorageProvider
import com.example.kmp.storage.StorageResult
import com.example.kmp.storage.StorageQuery
import com.example.kmp.storage.SortOrder
import com.example.kmp.template.config.AppConfig
import com.example.kmp.template.model.DemoEvent
import com.example.kmp.template.model.DemoRequest
import com.example.kmp.template.model.DemoResponse
import com.example.kmp.validation.ValidationService
import io.ktor.http.*
import io.ktor.server.plugins.*

class DemoService(
    private val storage: StorageProvider,
    private val messageBroker: MessageBroker,
    private val authProvider: AuthProvider,
    private val validationService: ValidationService
) {
    suspend fun processRequest(request: DemoRequest): DemoResponse {
        // Validate request
        validationService.validateOrThrow(request)
        
        // Store request
        storage.write(
            "${AppConfig.Storage.REQUESTS_STORE}:${request.id}",
            request
        )
        
        // Process request
        val response = DemoResponse(
            id = request.id,
            result = "Processed: ${request.data}"
        )
        
        // Store response
        storage.write(
            "${AppConfig.Storage.RESPONSES_STORE}:${response.id}",
            response
        )
        
        // Publish event
        val event = DemoEvent(
            correlationId = request.id,
            eventType = "REQUEST_PROCESSED",
            eventData = response.result
        )
        messageBroker.publish(AppConfig.Messaging.DEMO_EVENTS_TOPIC, event)
        
        return response
    }
    
    suspend fun getRequestHistory(): List<DemoRequest> {
        // Verify user has permission
        when (val result = authProvider.authorize("demo:read", setOf("demo:read"))) {
            is AuthResult.Success -> Unit
            is AuthResult.Error -> throw BadRequestException("Missing required permission: demo:read")
            else -> throw BadRequestException("Unknown authorization error")
        }
        
        // Query all requests
        val query = object : StorageQuery<DemoRequest> {
            override val filter = mapOf("key" to AppConfig.Storage.REQUESTS_STORE)
            override val sort = mapOf<String, SortOrder>()
            override val limit = null
            override val offset = null
        }
        
        return when (val result = storage.query(query)) {
            is StorageResult.Success<List<DemoRequest>> -> result.data ?: emptyList()
            is StorageResult.Error -> emptyList()
            else -> emptyList()
        }
    }
    
    suspend fun getResponseHistory(): List<DemoResponse> {
        // Verify user has permission
        when (val result = authProvider.authorize("demo:read", setOf("demo:read"))) {
            is AuthResult.Success -> Unit
            is AuthResult.Error -> throw BadRequestException("Missing required permission: demo:read")
            else -> throw BadRequestException("Unknown authorization error")
        }
        
        // Query all responses
        val query = object : StorageQuery<DemoResponse> {
            override val filter = mapOf("key" to AppConfig.Storage.RESPONSES_STORE)
            override val sort = mapOf<String, SortOrder>()
            override val limit = null
            override val offset = null
        }
        
        return when (val result = storage.query(query)) {
            is StorageResult.Success<List<DemoResponse>> -> result.data ?: emptyList()
            is StorageResult.Error -> emptyList()
            else -> emptyList()
        }
    }
} 