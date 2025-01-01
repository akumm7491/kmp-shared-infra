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
import org.slf4j.LoggerFactory

class DemoService(
    private val storage: StorageProvider,
    private val messageBroker: MessageBroker,
    private val authProvider: AuthProvider,
    private val validationService: ValidationService
) {
    private val logger = LoggerFactory.getLogger(DemoService::class.java)

    suspend fun processRequest(request: DemoRequest): DemoResponse {
        logger.info("Processing request: id=${request.id}")
        
        try {
            // Validate request
            logger.debug("Validating request: id=${request.id}")
            validationService.validateOrThrow(request)
            
            // Store request
            logger.debug("Storing request: id=${request.id}")
            storage.write(
                "${AppConfig.Storage.REQUESTS_STORE}:${request.id}",
                request
            ).also { result ->
                when (result) {
                    is StorageResult.Success -> logger.debug("Successfully stored request: id=${request.id}")
                    is StorageResult.Error -> logger.warn("Failed to store request: id=${request.id}, error=${result.message}")
                }
            }
            
            // Process request
            val response = DemoResponse(
                id = request.id,
                result = "Processed: ${request.data}"
            )
            
            // Store response
            logger.debug("Storing response: id=${response.id}")
            storage.write(
                "${AppConfig.Storage.RESPONSES_STORE}:${response.id}",
                response
            ).also { result ->
                when (result) {
                    is StorageResult.Success -> logger.debug("Successfully stored response: id=${response.id}")
                    is StorageResult.Error -> logger.warn("Failed to store response: id=${response.id}, error=${result.message}")
                }
            }
            
            // Publish event
            logger.debug("Publishing event for request: id=${request.id}")
            val event = DemoEvent(
                correlationId = request.id,
                eventType = "REQUEST_PROCESSED",
                eventData = response.result
            )
            messageBroker.publish(AppConfig.Messaging.DEMO_EVENTS_TOPIC, event)
            logger.debug("Successfully published event for request: id=${request.id}")
            
            logger.info("Successfully processed request: id=${request.id}")
            return response
        } catch (e: Exception) {
            logger.error("Failed to process request: id=${request.id}", e)
            throw e
        }
    }
    
    suspend fun getRequestHistory(): List<DemoRequest> {
        logger.info("Fetching request history")
        
        try {
            // Verify user has permission
            logger.debug("Verifying user permissions for request history")
            when (val result = authProvider.authorize("demo:read", setOf("demo:read"))) {
                is AuthResult.Success -> {
                    logger.debug("User authorized to view request history")
                }
                is AuthResult.Error -> {
                    logger.warn("User not authorized to view request history: ${result.message}")
                    throw BadRequestException("Missing required permission: demo:read")
                }
                else -> {
                    logger.error("Unknown authorization error when accessing request history")
                    throw BadRequestException("Unknown authorization error")
                }
            }
            
            // Query all requests
            logger.debug("Querying request history from storage")
            val query = object : StorageQuery<DemoRequest> {
                override val filter = mapOf("key" to AppConfig.Storage.REQUESTS_STORE)
                override val sort = mapOf<String, SortOrder>()
                override val limit = null
                override val offset = null
            }
            
            return when (val result = storage.query(query)) {
                is StorageResult.Success<List<DemoRequest>> -> {
                    val requests = result.data ?: emptyList()
                    logger.info("Successfully fetched request history: count=${requests.size}")
                    requests
                }
                is StorageResult.Error -> {
                    logger.warn("Failed to fetch request history: ${result.message}")
                    emptyList()
                }
                else -> {
                    logger.warn("Unknown result when fetching request history")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            logger.error("Error fetching request history", e)
            throw e
        }
    }
    
    suspend fun getResponseHistory(): List<DemoResponse> {
        logger.info("Fetching response history")
        
        try {
            // Verify user has permission
            logger.debug("Verifying user permissions for response history")
            when (val result = authProvider.authorize("demo:read", setOf("demo:read"))) {
                is AuthResult.Success -> {
                    logger.debug("User authorized to view response history")
                }
                is AuthResult.Error -> {
                    logger.warn("User not authorized to view response history: ${result.message}")
                    throw BadRequestException("Missing required permission: demo:read")
                }
                else -> {
                    logger.error("Unknown authorization error when accessing response history")
                    throw BadRequestException("Unknown authorization error")
                }
            }
            
            // Query all responses
            logger.debug("Querying response history from storage")
            val query = object : StorageQuery<DemoResponse> {
                override val filter = mapOf("key" to AppConfig.Storage.RESPONSES_STORE)
                override val sort = mapOf<String, SortOrder>()
                override val limit = null
                override val offset = null
            }
            
            return when (val result = storage.query(query)) {
                is StorageResult.Success<List<DemoResponse>> -> {
                    val responses = result.data ?: emptyList()
                    logger.info("Successfully fetched response history: count=${responses.size}")
                    responses
                }
                is StorageResult.Error -> {
                    logger.warn("Failed to fetch response history: ${result.message}")
                    emptyList()
                }
                else -> {
                    logger.warn("Unknown result when fetching response history")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            logger.error("Error fetching response history", e)
            throw e
        }
    }
} 