package com.example.kmp.api.gateway.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import com.example.kmp.api.gateway.discovery.ServiceInstance

interface ServiceClient {
    suspend fun forward(
        serviceInstance: ServiceInstance,
        path: String,
        originalRequest: ApplicationCall
    ): HttpResponse
}

class HttpServiceClient(private val client: HttpClient) : ServiceClient {
    override suspend fun forward(
        serviceInstance: ServiceInstance,
        path: String,
        originalRequest: ApplicationCall
    ): HttpResponse {
        return client.request("${serviceInstance.url}$path") {
            method = originalRequest.request.httpMethod
            headers.appendAll(originalRequest.request.headers)
            originalRequest.request.content.let { content ->
                setBody(content)
            }
        }
    }
} 