package com.example.kmp.validation

import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import jakarta.validation.ConstraintViolation
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.request.*
import kotlinx.coroutines.launch

/**
 * Core validation service that provides validation functionality using Jakarta Validation (formerly Bean Validation)
 */
class ValidationService {
    private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
    private val validator: Validator = factory.validator

    /**
     * Validates an object and returns any constraint violations
     */
    fun <T> validate(obj: T): Set<ConstraintViolation<T>> {
        return validator.validate(obj)
    }

    /**
     * Validates an object and throws a BadRequestException if there are any violations
     */
    fun <T> validateOrThrow(obj: T) {
        val violations = validate(obj)
        if (violations.isNotEmpty()) {
            throw BadRequestException(violations.joinToString(", ") { "${it.propertyPath}: ${it.message}" })
        }
    }

    /**
     * Extension function to validate a request body in Ktor
     */
    suspend inline fun <reified T> ApplicationCall.receiveAndValidate(): T {
        val obj = receiveNullable<T>() ?: throw BadRequestException("Request body cannot be null")
        ValidationService().validateOrThrow(obj)
        return obj
    }

    companion object {
        fun install(application: Application) {
            // Add validation status pages
            application.install(StatusPages) {
                exception<BadRequestException> { call, cause ->
                    application.launch {
                        call.respondText(
                            text = """
                                {
                                    "error": "Validation failed",
                                    "details": "${cause.message}",
                                    "timestamp": ${System.currentTimeMillis()}
                                }
                            """.trimIndent(),
                            contentType = ContentType.Application.Json,
                            status = HttpStatusCode.BadRequest
                        )
                    }
                }
            }
        }
    }
}

/**
 * Extension functions for common validation patterns
 */
fun String.validateEmail(): Boolean {
    return matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
}

fun String.validateUrl(): Boolean {
    return matches(Regex("^(http|https)://.*$"))
}

fun String.validateNotBlank(fieldName: String) {
    if (isBlank()) {
        throw BadRequestException("$fieldName cannot be blank")
    }
}

fun <T> T?.validateNotNull(fieldName: String) {
    if (this == null) {
        throw BadRequestException("$fieldName is required")
    }
}

/**
 * DSL for validation rules
 */
class ValidationScope<T> {
    private val rules = mutableListOf<(T) -> Unit>()

    fun rule(block: (T) -> Unit) {
        rules.add(block)
    }

    fun validate(value: T) {
        rules.forEach { it(value) }
    }
}

fun <T> validate(value: T, init: ValidationScope<T>.() -> Unit) {
    ValidationScope<T>().apply(init).validate(value)
}
