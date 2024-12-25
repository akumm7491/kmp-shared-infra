package com.example.kmp.testing.bdd

import io.cucumber.core.backend.ObjectFactory
import io.cucumber.core.runtime.TimeServiceEventBus
import io.cucumber.core.options.RuntimeOptionsBuilder
import io.cucumber.core.runtime.Runtime
import io.cucumber.plugin.event.*
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import java.time.Clock
import java.util.UUID
import java.net.URI

/**
 * BDD test runner for KMP services.
 * Provides Cucumber integration for behavior-driven development:
 * - Feature file parsing
 * - Step definition execution
 * - Test reporting
 * - Scenario management
 */
class KMPBDDRunner {
    private val steps = mutableMapOf<String, suspend () -> Unit>()
    private val hooks = mutableListOf<suspend () -> Unit>()
    private val results = mutableListOf<TestResult>()

    /**
     * Register step definition
     */
    fun Given(pattern: String, block: suspend () -> Unit) {
        steps[pattern] = block
    }

    fun When(pattern: String, block: suspend () -> Unit) {
        steps[pattern] = block
    }

    fun Then(pattern: String, block: suspend () -> Unit) {
        steps[pattern] = block
    }

    /**
     * Register before hook
     */
    fun Before(block: suspend () -> Unit) {
        hooks.add(block)
    }

    /**
     * Run BDD tests
     */
    fun runTests(): List<TestResult> {
        val eventBus = TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID)
        val options = RuntimeOptionsBuilder()
            .addGlue(URI("classpath:/com/example/kmp/testing/bdd"))
            .build()

        val objectFactory = KMPObjectFactory()
        System.setProperty("cucumber.object-factory", objectFactory.javaClass.name)

        val runtime = Runtime.builder()
            .withRuntimeOptions(options)
            .withEventBus(eventBus)
            .build()

        eventBus.registerHandlerFor(TestCaseStarted::class.java) { event ->
            runBlocking {
                hooks.forEach { it() }
            }
        }

        eventBus.registerHandlerFor(TestStepFinished::class.java) { event ->
            when (event.result.status) {
                Status.PASSED -> results.add(TestResult.Success(event.testStep.toString()))
                Status.FAILED -> results.add(TestResult.Failure(event.testStep.toString(), event.result.error ?: RuntimeException("Unknown error")))
                else -> results.add(TestResult.Skipped(event.testStep.toString()))
            }
        }

        runtime.run()
        return results
    }

    /**
     * Object factory for dependency injection
     */
    class KMPObjectFactory : ObjectFactory {
        private val instances = mutableMapOf<Class<*>, Any>()

        override fun <T : Any> getInstance(glueClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return instances.getOrPut(glueClass) {
                glueClass.getDeclaredConstructor().newInstance()
            } as T
        }

        override fun start() {
            // Initialize factory
        }

        override fun stop() {
            instances.clear()
        }

        override fun addClass(glueClass: Class<*>): Boolean {
            // Register glue class
            return true
        }
    }

    /**
     * Test result model
     */
    sealed class TestResult {
        data class Success(val step: String) : TestResult()
        data class Failure(val step: String, val error: Throwable) : TestResult()
        data class Skipped(val step: String) : TestResult()
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(KMPBDDRunner::class.java)

        /**
         * Run BDD tests for a service
         */
        fun runServiceTests(serviceClass: KClass<*>, featuresPath: String): List<TestResult> {
            val runner = KMPBDDRunner()
            
            // Register service-specific steps
            serviceClass.java.methods
                .filter { it.isAnnotationPresent(StepDefinition::class.java) }
                .forEach { method ->
                    val annotation = method.getAnnotation(StepDefinition::class.java)
                    runner.steps[annotation.pattern] = {
                        runBlocking {
                            method.invoke(serviceClass.java.getDeclaredConstructor().newInstance())
                        }
                    }
                }
            
            // Run tests
            return runner.runTests()
        }
    }
}

/**
 * Step definition annotation
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class StepDefinition(val pattern: String)
