package com.example.kmp.networking.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import com.example.kmp.networking.models.ApiResponse
import com.example.kmp.networking.resilience.RetryConfig
import com.example.kmp.networking.resilience.retry

suspend inline fun <reified T> HttpClient.get(
    url: String,
    retryConfig: RetryConfig = RetryConfig(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): ApiResponse<T> = retry(retryConfig) {
    request {
        method = HttpMethod.Get
        url(url)
        apply(builder)
    }.body()
}

suspend inline fun <reified T> HttpClient.post(
    url: String,
    body: Any? = null,
    retryConfig: RetryConfig = RetryConfig(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): ApiResponse<T> = retry(retryConfig) {
    request {
        method = HttpMethod.Post
        url(url)
        setBody(body)
        apply(builder)
    }.body()
}

suspend inline fun <reified T> HttpClient.put(
    url: String,
    body: Any? = null,
    retryConfig: RetryConfig = RetryConfig(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): ApiResponse<T> = retry(retryConfig) {
    request {
        method = HttpMethod.Put
        url(url)
        setBody(body)
        apply(builder)
    }.body()
}

suspend inline fun <reified T> HttpClient.delete(
    url: String,
    retryConfig: RetryConfig = RetryConfig(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): ApiResponse<T> = retry(retryConfig) {
    request {
        method = HttpMethod.Delete
        url(url)
        apply(builder)
    }.body()
}
