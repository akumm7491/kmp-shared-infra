package com.example.kmp.api.gateway.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.utils.io.*
import com.example.kmp.api.gateway.discovery.ServiceInstance
import org.slf4j.LoggerFactory

interface ServiceClient {
    suspend fun forward(
        serviceInstance: ServiceInstance,
        path: String,
        originalRequest: ApplicationCall
    ): HttpResponse
}

class HttpServiceClient(private val client: HttpClient) : ServiceClient {
    private val logger = LoggerFactory.getLogger(HttpServiceClient::class.java)

    override suspend fun forward(
        serviceInstance: ServiceInstance,
        path: String,
        originalRequest: ApplicationCall
    ): HttpResponse {
        val url = "${serviceInstance.url}$path"
        logger.debug("Forwarding request to: $url")

        return client.request(url) {
            method = originalRequest.request.httpMethod
            headers {
                originalRequest.request.headers.forEach { name, values ->
                    values.forEach { value ->
                        append(name, value)
                    }
                }
            }
            
            if (originalRequest.request.httpMethod != HttpMethod.Get) {
                try {
                    val requestBody = originalRequest.receiveText()
                    if (requestBody.isNotEmpty()) {
                        setBody(requestBody)
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to read request body: ${e.message}")
                }
            }
        }
    }
}
