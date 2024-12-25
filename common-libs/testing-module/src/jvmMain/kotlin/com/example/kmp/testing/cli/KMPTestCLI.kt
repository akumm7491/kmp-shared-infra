package com.example.kmp.testing.cli

import org.apache.commons.cli.*
import kotlin.system.exitProcess

/**
 * Base CLI tool for KMP service tests.
 * Provides common command-line functionality:
 * - Option parsing
 * - Help generation
 * - Error handling
 * - Configuration management
 */
abstract class KMPTestCLI {
    protected abstract val serviceName: String
    protected abstract val runner: Any

    /**
     * Create command-line options
     */
    protected open fun createOptions(): Options = Options().apply {
        // Common options
        addOption(Option.builder("h")
            .longOpt("help")
            .desc("Show help message")
            .build())
        
        addOption(Option.builder("t")
            .longOpt("tag")
            .hasArg()
            .argName("TAG")
            .desc("Run tests with specific tag")
            .build())
        
        addOption(Option.builder("p")
            .longOpt("pattern")
            .hasArg()
            .argName("PATTERN")
            .desc("Run tests matching pattern")
            .build())
        
        addOption(Option.builder("c")
            .longOpt("class")
            .hasArgs()
            .argName("CLASS")
            .desc("Run specific test classes")
            .build())
        
        addOption(Option.builder("r")
            .longOpt("report-dir")
            .hasArg()
            .argName("DIR")
            .desc("Test report output directory")
            .build())

        // Test suite options
        addOption(Option.builder("u")
            .longOpt("unit")
            .desc("Run unit tests")
            .build())

        addOption(Option.builder("i")
            .longOpt("integration")
            .desc("Run integration tests")
            .build())

        addOption(Option.builder("perf")
            .longOpt("performance")
            .desc("Run performance tests")
            .build())

        // Configuration options
        addOption(Option.builder("config")
            .longOpt("config-path")
            .hasArg()
            .argName("PATH")
            .desc("Custom configuration file path")
            .build())

        addOption(Option.builder("log")
            .longOpt("log-level")
            .hasArg()
            .argName("LEVEL")
            .desc("Logging level (INFO, DEBUG, etc.)")
            .build())

        addOption(Option.builder("timeout")
            .longOpt("timeout")
            .hasArg()
            .argName("SECONDS")
            .desc("Test execution timeout")
            .type(Number::class.java)
            .build())

        addOption(Option.builder("parallel")
            .longOpt("parallel")
            .hasArg()
            .argName("THREADS")
            .desc("Number of parallel test threads")
            .type(Number::class.java)
            .build())
    }

    /**
     * Run CLI tool
     */
    fun run(args: Array<String>) {
        val options = createOptions()

        try {
            val cmd = DefaultParser().parse(options, args)

            if (cmd.hasOption("help")) {
                printHelp(options)
                return
            }

            // Configure test execution
            configureTests(cmd)

            // Run tests based on options
            when {
                cmd.hasOption("tag") -> {
                    val tag = cmd.getOptionValue("tag")
                    println("Running tests with tag: $tag")
                    executeRunner("runTestsByTag", tag)
                }
                
                cmd.hasOption("pattern") -> {
                    val pattern = cmd.getOptionValue("pattern")
                    println("Running tests matching pattern: $pattern")
                    executeRunner("runTestsByPattern", pattern)
                }
                
                cmd.hasOption("class") -> {
                    val classNames = cmd.getOptionValues("class")
                    println("Running specific test classes: ${classNames.joinToString()}")
                    val classes = classNames.map { Class.forName(it) }.toTypedArray()
                    executeRunner("runTests", classes)
                }

                cmd.hasOption("unit") -> {
                    println("Running unit tests")
                    executeRunner("runUnitTests")
                }

                cmd.hasOption("integration") -> {
                    println("Running integration tests")
                    executeRunner("runIntegrationTests")
                }

                cmd.hasOption("performance") -> {
                    println("Running performance tests")
                    executeRunner("runPerformanceTests")
                }
                
                else -> {
                    println("Running all tests")
                    executeRunner("runTests")
                }
            }
        } catch (e: ParseException) {
            System.err.println("Error parsing arguments: ${e.message}")
            printHelp(options)
            exitProcess(1)
        } catch (e: Exception) {
            System.err.println("Error running tests: ${e.message}")
            e.printStackTrace()
            exitProcess(1)
        }
    }

    /**
     * Configure test execution
     */
    protected open fun configureTests(cmd: CommandLine) {
        // Configure report directory
        cmd.getOptionValue("report-dir")?.let { dir ->
            System.setProperty("test.report.dir", dir)
        }

        // Configure custom settings
        cmd.getOptionValue("config")?.let { path ->
            System.setProperty("test.config.path", path)
        }

        cmd.getOptionValue("log")?.let { level ->
            System.setProperty("test.log.level", level)
        }

        cmd.getOptionValue("timeout")?.let { timeout ->
            System.setProperty("test.timeout", timeout)
        }

        cmd.getOptionValue("parallel")?.let { threads ->
            System.setProperty("test.parallel.threads", threads)
        }
    }

    /**
     * Print help message
     */
    protected open fun printHelp(options: Options) {
        HelpFormatter().printHelp(
            "$serviceName Tests",
            """
            Test Runner
            Run tests with various options and generate reports.
            
            Examples:
              Run all tests:
                $serviceName-tests
              
              Run tests with tag:
                $serviceName-tests -t integration
              
              Run tests matching pattern:
                $serviceName-tests -p "*IntegrationTest"
              
              Run specific test classes:
                $serviceName-tests -c com.example.MyTest
              
              Specify report directory:
                $serviceName-tests -r build/reports/custom
              
              Run with custom config:
                $serviceName-tests --config path/to/config.conf
              
              Run with custom logging:
                $serviceName-tests --log DEBUG
              
              Run with timeout:
                $serviceName-tests --timeout 300
              
              Run in parallel:
                $serviceName-tests --parallel 4
            """.trimIndent(),
            options,
            "\nReport bugs to: https://github.com/your-org/kmp-shared-infra/issues"
        )
    }

    /**
     * Execute runner method
     */
    protected fun executeRunner(method: String, vararg args: Any?) {
        runner::class.java.getMethod(method, *args.map { it?.javaClass ?: Any::class.java }.toTypedArray())
            .invoke(runner, *args)
    }
}
