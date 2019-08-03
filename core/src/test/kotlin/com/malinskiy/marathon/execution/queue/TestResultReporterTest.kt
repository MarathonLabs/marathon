package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.createDeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.generateTest
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.TestVendorConfiguration
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.amshove.kluent.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File

object TestResultReporterSpec : Spek({

    val defaultConfig = Configuration(name = "",
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
            strictMode = null,
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
    val strictConfig = defaultConfig.copy(strictMode = true)
    val analytics = mock(Analytics::class)
    val test = generateTest()
    val poolId = DevicePoolId("test")

    fun filterDefault() = TestResultReporter(poolId,
            analytics,
            TestShard(listOf(test,test,test)),
            defaultConfig)

    fun filterStrict() = TestResultReporter(poolId,
            analytics,
            TestShard(listOf(test,test,test)),
            strictConfig)
    val deviceInfo = createDeviceInfo()

    afterEachTest {
        reset(analytics)
    }

    given("a reporter with a default config") {
        on("success - failure - failure") {
            it("should report success") {
                val filter = filterDefault()

                val r1 = TestResult(test, deviceInfo, TestStatus.PASSED, 0, 1)
                val r2 = TestResult(test, deviceInfo, TestStatus.FAILURE, 2, 3)
                val r3 = TestResult(test, deviceInfo, TestStatus.FAILURE, 4, 5)

                filter.testFinished(deviceInfo, r1)
                filter.testFailed(deviceInfo, r2)
                filter.testFailed(deviceInfo, r3)

                inOrder(analytics) {
                    verify(analytics).trackTestFinished(poolId, deviceInfo, r1)
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r1)
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r2)
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r3)
                    verifyNoMoreInteractions(analytics)
                }
            }
        }

        on("failure - failure - success") {
            it("should report success") {
                val filter = filterDefault()

                val r1 = TestResult(test, deviceInfo, TestStatus.FAILURE, 0, 1)
                val r2 = TestResult(test, deviceInfo, TestStatus.FAILURE, 2, 3)
                val r3 = TestResult(test, deviceInfo, TestStatus.PASSED, 4, 5)

                filter.testFailed(deviceInfo, r1)
                filter.testFailed(deviceInfo, r2)
                filter.testFinished(deviceInfo, r3)

                inOrder(analytics) {
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r1)
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r2)
                    verify(analytics).trackTestFinished(poolId, deviceInfo, r3)
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r3)
                    verifyNoMoreInteractions(analytics)
                }
            }
        }
    }

    given("a reporter with a strict config") {
        on("success - failure - failure") {
            it("should report failure") {
                val filter = filterStrict()

                val r1 = TestResult(test, deviceInfo, TestStatus.PASSED, 0, 1)
                val r2 = TestResult(test, deviceInfo, TestStatus.FAILURE, 2, 3)
                val r3 = TestResult(test, deviceInfo, TestStatus.FAILURE, 4, 5)

                filter.testFinished(deviceInfo, r1)
                filter.testFailed(deviceInfo, r2)
                filter.testFailed(deviceInfo, r3)

                inOrder(analytics) {
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r1)
                    verify(analytics).trackTestFinished(poolId, deviceInfo, r2)
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r2)
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r3)
                    verifyNoMoreInteractions(analytics)
                }
            }
        }

        on("failure - success - success") {
            it("should report failure") {
                val filter = filterStrict()

                val r1 = TestResult(test, deviceInfo, TestStatus.FAILURE, 0, 1)
                val r2 = TestResult(test, deviceInfo, TestStatus.PASSED, 2, 3)
                val r3 = TestResult(test, deviceInfo, TestStatus.PASSED, 4, 5)

                filter.testFailed(deviceInfo, r1)
                filter.testFinished(deviceInfo, r2)
                filter.testFinished(deviceInfo, r3)

                inOrder(analytics) {
                    verify(analytics).trackTestFinished(poolId, deviceInfo, r1)
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r1)
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r2)
                    verify(analytics).trackRawTestRun(poolId, deviceInfo, r3)
                    verifyNoMoreInteractions(analytics)
                }
            }
        }
    }
})
