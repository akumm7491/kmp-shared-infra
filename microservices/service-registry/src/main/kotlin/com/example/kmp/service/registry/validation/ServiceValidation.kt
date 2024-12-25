package com.example.kmp.service.registry.validation

import com.example.kmp.service.registry.model.RegistrationRequest
import com.example.kmp.service.registry.model.ServiceInstance
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import jakarta.validation.Validation

/**
 * Service validation utilities.
 * Provides validation for service registration and updates.
 */
object ServiceValidation {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    /**
     * Validate registration request
     */
    fun validateRegistration(request: RegistrationRequest): Either<List<String>, ServiceInstance> {
        val violations = validator.validate(request)
        return if (violations.isEmpty()) {
            request.toServiceInstance().right()
        } else {
            violations.map { "${it.propertyPath}: ${it.message}" }.toList().left()
        }
    }

    /**
     * Validate service instance
     */
    fun validateInstance(instance: ServiceInstance): Either<List<String>, ServiceInstance> {
        val violations = validator.validate(instance)
        return if (violations.isEmpty()) {
            instance.right()
        } else {
            violations.map { "${it.propertyPath}: ${it.message}" }.toList().left()
        }
    }

    /**
     * Validate status update
     */
    fun validateStatusUpdate(instance: ServiceInstance): Either<List<String>, ServiceInstance> {
        val violations = validator.validate(instance)
        return if (violations.isEmpty()) {
            instance.right()
        } else {
            violations.map { "${it.propertyPath}: ${it.message}" }.toList().left()
        }
    }

    /**
     * Get validation errors
     */
    fun getValidationErrors(instance: ServiceInstance): List<String> {
        return validator.validate(instance).map { violation ->
            "${violation.propertyPath}: ${violation.message}"
        }
    }
}
