package com.example.kmp.messaging.schema

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.schemaregistry.avro.AvroSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure
import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.slf4j.LoggerFactory

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegisterSchema(
    val topic: String,
    val name: String = "",
    val namespace: String = ""
)

object SchemaRegistry {
    private val logger = LoggerFactory.getLogger(SchemaRegistry::class.java)
    private lateinit var client: SchemaRegistryClient
    private val registeredSchemas = mutableSetOf<KClass<*>>()
    
    fun initialize(url: String) {
        client = CachedSchemaRegistryClient(url, 100)
    }
    
    fun registerSchema(kClass: KClass<*>) {
        try {
            if (registeredSchemas.contains(kClass)) {
                logger.debug("Schema for ${kClass.simpleName} is already registered")
                return
            }
            
            val annotation = kClass.findAnnotation<RegisterSchema>() 
                ?: throw IllegalArgumentException("Class ${kClass.simpleName} must be annotated with @RegisterSchema")
                
            logger.info("Registering schema for ${kClass.simpleName} with topic ${annotation.topic}")
            
            val schema = generateAvroSchema(kClass, annotation)
            val subject = "${annotation.topic}-value"
            
            val schemaId = client.register(subject, AvroSchema(schema))
            registeredSchemas.add(kClass)
            logger.info("Successfully registered schema for ${kClass.simpleName} with topic ${annotation.topic}, ID: $schemaId")
        } catch (e: Exception) {
            logger.error("Failed to register schema for ${kClass.simpleName}", e)
            throw RuntimeException("Failed to register schema for ${kClass.simpleName}", e)
        }
    }
    
    private fun generateAvroSchema(kClass: KClass<*>, annotation: RegisterSchema): Schema {
        val builder = SchemaBuilder.record(
            annotation.name.ifEmpty { kClass.simpleName }
        ).namespace(
            annotation.namespace.ifEmpty { kClass.java.`package`.name }
        )
        
        val fields = builder.fields()
        
        kClass.declaredMemberProperties.forEach { prop ->
            val field = when (prop.returnType.jvmErasure) {
                String::class -> fields.requiredString(prop.name)
                Int::class -> fields.requiredInt(prop.name)
                Long::class -> fields.requiredLong(prop.name)
                Double::class -> fields.requiredDouble(prop.name)
                Boolean::class -> fields.requiredBoolean(prop.name)
                else -> throw IllegalArgumentException("Unsupported type for property ${prop.name}")
            }
        }
        
        return fields.endRecord()
    }
}
