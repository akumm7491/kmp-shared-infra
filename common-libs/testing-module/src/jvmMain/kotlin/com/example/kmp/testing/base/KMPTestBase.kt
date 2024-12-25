package com.example.kmp.testing.base

import io.cucumber.junit.platform.engine.Cucumber
import io.ktor.server.testing.*
import io.mockk.MockKAnnotations
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import arrow.core.Either
import arrow.core.raise.either
import com.example.kmp.testing.bdd.KMPBDDRunner
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Base class for all KMP service tests.
 * Provides common testing functionality:
 * - BDD support with Cucumber
 * - Functional testing with Arrow
 * - Ktor test utilities
 * - Mocking support
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class KMPTestBase {
    
    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        setupTest()
    }

    /**
     * Override to provide test-specific setup
     */
    protected open fun setupTest() {}

    /**
     * Run test with Ktor test application
     */
    protected fun withTestApp(test: suspend ApplicationTestBuilder.() -> Unit) {
        testApplication {
            test()
        }
    }

    /**
     * Run test with error handling
     */
    protected suspend fun <T> runTest(block: suspend () -> T): Either<Throwable, T> = either {
        try {
            block()
        } catch (e: Throwable) {
            raise(e)
        }
    }

    /**
     * Verify test result
     */
    protected fun <T> verifyResult(result: Either<Throwable, T>, verify: (T) -> Unit) {
        result.fold(
            { throw it },
            { verify(it) }
        )
    }

    /**
     * Test data builder
     */
    protected fun <T> buildTestData(builder: () -> T): T = builder()

    /**
     * BDD test runner
     */
    private val bddRunner = KMPBDDRunner()

    /**
     * Run BDD scenario
     */
    protected fun scenario(name: String, block: suspend () -> Unit) {
        logger.debug("Starting scenario: $name")
        bddRunner.Given(name) {
            runBlocking {
                try {
                    block()
                } catch (e: Exception) {
                    logger.error("Error in scenario: $name", e)
                    throw e
                }
            }
        }
    }

    /**
     * Run test step
     */
    protected fun step(name: String, block: suspend () -> Unit) {
        logger.debug("Executing step: $name")
        bddRunner.When(name) {
            runBlocking {
                try {
                    block()
                } catch (e: Exception) {
                    logger.error("Error in step: $name", e)
                    throw e
                }
            }
        }
    }

    /**
     * Run verification step
     */
    protected fun verify(name: String, block: suspend () -> Unit) {
        logger.debug("Verifying: $name")
        bddRunner.Then(name) {
            runBlocking {
                try {
                    block()
                } catch (e: Exception) {
                    logger.error("Error in verification: $name", e)
                    throw e
                }
            }
        }
    }

    /**
     * Run all BDD tests
     */
    protected fun runBDDTests(): List<KMPBDDRunner.TestResult> {
        logger.info("Running all BDD tests")
        return try {
            bddRunner.runTests()
        } catch (e: Exception) {
            logger.error("Error running BDD tests", e)
            listOf(KMPBDDRunner.TestResult.Failure("BDD Test Execution", e))
        }
    }

    companion object {
        private val logger = org.slf4j.LoggerFactory.getLogger(KMPTestBase::class.java)

        /**
         * Test configuration
         */
        object Config {
            const val TEST_MODE = true
            const val DEFAULT_TIMEOUT = 5000L
            const val PARALLEL_TESTS = true
            const val RETRY_COUNT = 3
        }
    }
}
