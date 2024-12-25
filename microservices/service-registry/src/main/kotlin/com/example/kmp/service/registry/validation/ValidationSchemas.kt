package com.example.kmp.service.registry.validation

import com.example.kmp.service.registry.model.ServiceInstance
import jakarta.validation.Validation
import jakarta.validation.Validator
import arrow.core.Either
import arrow.core.left
import arrow.core.right

/**
 * Validation schemas for service registry requests and responses
 */
object ValidationSchemas {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator

    /**
     * Schema for service registration request
     */
    fun validateRegistrationRequest(instance: ServiceInstance): Either<List<String>, ServiceInstance> {
        val violations = validator.validate(instance)
        return if (violations.isEmpty()) {
            instance.right()
        } else {
            violations.map { "${it.propertyPath}: ${it.message}" }.left()
        }
    }

    /**
     * Schema for service instance
     */
    fun validateServiceInstance(instance: ServiceInstance): Either<List<String>, ServiceInstance> {
        val violations = validator.validate(instance)
        return if (violations.isEmpty()) {
            instance.right()
        } else {
            violations.map { "${it.propertyPath}: ${it.message}" }.left()
        }
    }

    /**
     * Schema for status update
     */
    fun validateStatusUpdate(instance: ServiceInstance): Either<List<String>, ServiceInstance> {
        val violations = validator.validate(instance)
        return if (violations.isEmpty()) {
            instance.right()
        } else {
            violations.map { "${it.propertyPath}: ${it.message}" }.left()
        }
    }

    /**
     * Get validation errors for an instance
     */
    fun getValidationErrors(instance: ServiceInstance): List<String> {
        return validator.validate(instance).map { violation ->
            "${violation.propertyPath}: ${violation.message}"
        }
    }
}
