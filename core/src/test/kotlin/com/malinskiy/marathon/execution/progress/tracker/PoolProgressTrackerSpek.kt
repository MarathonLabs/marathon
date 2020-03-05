package com.malinskiy.marathon.execution.progress.tracker

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestVendorConfiguration
import org.amshove.kluent.shouldEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File

class PoolProgressTrackerSpek : Spek(
    {
        val test = Test(
            pkg = "com.malinskiy.marathon",
            clazz = "SomeTest",
            method = "someMethod",
            metaProperties = emptyList()
        )

        fun createConfiguration(strictMode: Boolean): Configuration {
            return Configuration(
                name = "",
                outputDir = File(""),
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
                strictMode = strictMode,
                uncompletedTestRetryQuota = null,
                testClassRegexes = null,
                includeSerialRegexes = null,
                excludeSerialRegexes = null,
                testBatchTimeoutMillis = null,
                testOutputTimeoutMillis = null,
                debug = false,
                vendorConfiguration = TestVendorConfiguration(Mocks.TestParser.DEFAULT, StubDeviceProvider()),
                analyticsTracking = false
            )
        }

        given("Configuration strictMode false") {
            val unit = PoolProgressTracker(createConfiguration(strictMode = false))
            on("Test started, passed and then failed") {
                unit.testStarted(test)
                unit.testPassed(test)
                unit.testFailed(test)
                it("Produces true") {
                    unit.aggregateResult().shouldEqualTo(true)
                }
            }
            on("Test passed again") {
                unit.testPassed(test)
                it("Produces true") {
                    unit.aggregateResult().shouldEqualTo(true)
                }
            }
        }

        given("Configuration strictMode true") {
            val unit = PoolProgressTracker(createConfiguration(strictMode = true))
            on("Test started, passed and then failed") {
                unit.testStarted(test)
                unit.testPassed(test)
                unit.testFailed(test)
                it("Produces false") {
                    unit.aggregateResult().shouldEqualTo(false)
                }
            }
            on("Test passed again") {
                unit.testPassed(test)
                it("Produces false") {
                    unit.aggregateResult().shouldEqualTo(false)
                }
            }
        }
    })
