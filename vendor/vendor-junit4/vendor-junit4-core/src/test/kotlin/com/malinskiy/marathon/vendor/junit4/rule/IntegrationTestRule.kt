package com.malinskiy.marathon.vendor.junit4.rule

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import com.malinskiy.marathon.vendor.junit4.configuration.executor.LocalExecutorConfiguration
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class IntegrationTestRule(private val temp: TemporaryFolder) : TestRule {
    lateinit var configuration: Configuration

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val deps = listOf(
                    "/junit4-integration-tests.jar",
                    "/fixtures/junit4/junit-4.13.2.jar",
                    "/fixtures/junit4/hamcrest-core-1.3.jar",
                    "/fixtures/junit4/kotlin-stdlib-common-1.4.31.jar",
                    "/fixtures/junit4/kotlin-stdlib-1.4.31.jar",
                    "/fixtures/junit4/kotlin-stdlib-jdk7-1.4.31.jar",
                    "/fixtures/junit4/kotlin-stdlib-jdk8-1.4.31.jar",
                ).map {
                    temp.newFile().apply {
                        outputStream().use { tempFile ->
                            javaClass.getResourceAsStream(it).copyTo(tempFile)
                        }
                    }
                }

                val junit4Configuration = Junit4Configuration(
                    applicationClasspath = null,
                    testClasspath = deps,
                    testBundles = null,
                    testPackageRoot = "com.malinskiy.marathon.vendor.junit4.integrationtests",
                    debugBooter = false,
                    forkEvery = 0,
                    executorConfiguration = LocalExecutorConfiguration(parallelism = 1, debug = false)
                )

                configuration = Configuration(
                    name = "junit4 test configuration",
                    outputDir = temp.newFolder("marathon-report"),
                    analyticsConfiguration = null,
                    poolingStrategy = null,
                    shardingStrategy = null,
                    sortingStrategy = null,
                    batchingStrategy = null,
                    flakinessStrategy = null,
                    retryStrategy = null,
                    filteringConfiguration = null,
                    ignoreFailures = null,
                    isCodeCoverageEnabled = null,
                    fallbackToScreenshots = null,
                    strictMode = null,
                    uncompletedTestRetryQuota = null,
                    testClassRegexes = null,
                    includeSerialRegexes = null,
                    excludeSerialRegexes = null,
                    testBatchTimeoutMillis = null,
                    debug = true,
                    testOutputTimeoutMillis = null,
                    screenRecordingPolicy = null,
                    vendorConfiguration = junit4Configuration,
                    analyticsTracking = null,
                    deviceInitializationTimeoutMillis = null,
                )

                base.evaluate()
            }

            private fun killAll() {
                killOnPort(1044)
                killOnPort(1045)
                killOnPort(50051)
            }

            private fun killOnPort(port: Int) {
                val listProcess = Runtime.getRuntime().exec("lsof -t -i :$port")
                val output = listProcess.inputStream.bufferedReader().readText()
                listProcess.waitFor()
                val pid = output.trim()
                if (pid.isNotEmpty()) {
                    Runtime.getRuntime().exec("kill $pid").waitFor()
                }
            }
        }
    }
}
