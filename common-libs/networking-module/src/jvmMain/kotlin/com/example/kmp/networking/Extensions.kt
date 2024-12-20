package com.example.kmp.networking

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import com.example.kmp.networking.models.ApiError
import com.example.kmp.networking.models.ApiResponse

suspend inline fun <reified T> ApplicationCall.respondSuccess(
    data: T,
    status: HttpStatusCode = HttpStatusCode.OK,
    metadata: Map<String, String> = emptyMap()
) {
    respond(status, ApiResponse(data = data, metadata = metadata))
}

suspend fun ApplicationCall.respondError(
    error: ApiError,
    status: HttpStatusCode = HttpStatusCode.BadRequest
) {
    respond(status, ApiResponse<Nothing>(error = error))
}

// Common error responses
fun ApiError.Companion.notFound(message: String = "Resource not found") = ApiError(
    code = "NOT_FOUND",
    message = message
)

fun ApiError.Companion.badRequest(message: String) = ApiError(
    code = "BAD_REQUEST",
    message = message
)

fun ApiError.Companion.unauthorized(message: String = "Unauthorized") = ApiError(
    code = "UNAUTHORIZED",
    message = message
)

fun ApiError.Companion.internalError(message: String = "Internal server error") = ApiError(
    code = "INTERNAL_ERROR",
    message = message
)
