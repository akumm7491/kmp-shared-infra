package com.example.kmp.networking

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

suspend inline fun <reified T> HttpClient.get(
    url: String,
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = request {
        method = HttpMethod.Get
        url(url)
        builder()
    }
    return response.body()
}

suspend inline fun <reified T> HttpClient.post(
    url: String,
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = request {
        method = HttpMethod.Post
        url(url)
        builder()
    }
    return response.body()
}

suspend inline fun <reified T> HttpClient.put(
    url: String,
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = request {
        method = HttpMethod.Put
        url(url)
        builder()
    }
    return response.body()
}

suspend inline fun <reified T> HttpClient.delete(
    url: String,
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): T {
    val response = request {
        method = HttpMethod.Delete
        url(url)
        builder()
    }
    return response.body()
}

suspend inline fun <reified T> HttpClient.executeWithResilience(
    crossinline block: suspend HttpClient.() -> HttpResponse
): T {
    val response = block()
    return response.body()
}
