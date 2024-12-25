package com.example.kmp.validation

/**
 * Core validation functionality
 */
object Validation {
    fun <T> validate(value: T, schema: ValidationSchema<T>): T {
        schema.validate(value)
        return value
    }
}

/**
 * Schema for validating data structures
 */
class ValidationSchema<T> private constructor(
    private val fields: List<Field<T, *>>,
    private val rules: List<ValidationRule<T>>
) {
    companion object {
        operator fun <T> invoke(init: SchemaBuilder<T>.() -> Unit): ValidationSchema<T> {
            val builder = SchemaBuilder<T>()
            builder.init()
            return ValidationSchema(builder.fields, builder.rules)
        }
    }

    fun validate(value: T) {
        rules.forEach { it.validate(value) }
        fields.forEach { it.validate(value) }
    }
}

/**
 * Builder for creating validation schemas
 */
class SchemaBuilder<T> {
    internal val fields = mutableListOf<Field<T, *>>()
    internal val rules = mutableListOf<ValidationRule<T>>()

    fun <V> field(name: String, extractor: (T) -> V, init: FieldBuilder<V>.() -> Unit) {
        val builder = FieldBuilder<V>()
        builder.init()
        fields.add(Field(name, extractor, builder.rules))
    }

    fun <V> field(name: String, schema: ValidationSchema<V>, extractor: (T) -> V) {
        fields.add(Field(name, extractor, listOf(ValidationRule("Schema validation") { value ->
            schema.validate(value)
            true
        })))
    }

    fun rule(message: String, predicate: (T) -> Boolean) {
        rules.add(ValidationRule(message, predicate))
    }
}

/**
 * Builder for creating field validations
 */
class FieldBuilder<T> {
    internal val rules = mutableListOf<ValidationRule<T>>()

    fun rule(message: String, predicate: (T) -> Boolean) {
        rules.add(ValidationRule(message, predicate))
    }
}

/**
 * Field definition with validation rules
 */
class Field<T, V>(
    private val name: String,
    private val extractor: (T) -> V,
    private val rules: List<ValidationRule<V>>
) {
    fun validate(value: T) {
        val fieldValue = extractor(value)
        rules.forEach { rule ->
            if (!rule.predicate(fieldValue)) {
                throw ValidationException("Field '$name': ${rule.message}")
            }
        }
    }
}

/**
 * Individual validation rule
 */
class ValidationRule<T>(
    val message: String,
    val predicate: (T) -> Boolean
) {
    fun validate(value: T) {
        if (!predicate(value)) {
            throw ValidationException(message)
        }
    }
}

/**
 * Exception thrown when validation fails
 */
class ValidationException(message: String) : RuntimeException(message)
