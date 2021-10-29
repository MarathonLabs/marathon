package com.malinskiy.marathon.execution.progress

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.test.StubDevice
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File
import com.malinskiy.marathon.test.Test as MarathonTest

class ProgressReporterTest {
    private val reporter = ProgressReporter(
        Configuration.Builder(
            name = "",
            outputDir = File(""),
            vendorConfiguration = VendorConfiguration.StubVendorConfiguration,
        ).apply {
            debug = false
            analyticsTracking = false
        }.build()
    )
    private val deviceInfo = StubDevice().toDeviceInfo()

    @Test
    fun shouldReportProgressForOnePool() {
        val poolId = DevicePoolId("testpool")

        val test1 = MarathonTest("com.example", "SimpleTest", "method1", emptyList())
        val test2 = MarathonTest("com.example", "SimpleTest", "method2", emptyList())
        val test3 = MarathonTest("com.example", "SimpleTest", "method3", emptyList())

        reporter.testCountExpectation(poolId, 3)
        reporter.progress().shouldBeEqualTo(.0f)

        /**
         * test 1 passed
         */
        reporter.testStarted(poolId, deviceInfo, test1)
        reporter.testPassed(poolId, deviceInfo, test1)
        reporter.progress().shouldBeEqualTo(1 / 3f)

        /**
         * test 2 failed
         */
        reporter.testStarted(poolId, deviceInfo, test2)
        reporter.testFailed(poolId, deviceInfo, test2)
        reporter.progress().shouldBeEqualTo(2 / 3f)

        /**
         * adding 4 retries for test2 and then test 2 passes once
         */
        reporter.addRetries(poolId, 4)
        reporter.progress().shouldBeEqualTo(2 / 7f)
        reporter.testStarted(poolId, deviceInfo, test2)
        reporter.testPassed(poolId, deviceInfo, test2)
        reporter.progress().shouldBeEqualTo(3 / 7f)

        /**
         * 1 retry of test 2 fails
         */
        reporter.testStarted(poolId, deviceInfo, test2)
        reporter.testFailed(poolId, deviceInfo, test2)
        reporter.progress().shouldBeEqualTo(4 / 7f)

        /**
         * 1 retry of test 2 is ignored
         */
        reporter.testStarted(poolId, deviceInfo, test2)
        reporter.testIgnored(poolId, deviceInfo, test2)
        reporter.progress().shouldBeEqualTo(5 / 7f)

        /**
         * removing one retry of test 2
         */
        reporter.removeTests(poolId, 1)
        reporter.progress().shouldBeEqualTo(5 / 6f)

        /**
         * test 3 is ignored (assumption failure or just ignore)
         */
        reporter.testStarted(poolId, deviceInfo, test3)
        reporter.testIgnored(poolId, deviceInfo, test3)
        val progress = reporter.progress()
        progress.shouldBeEqualTo(6 / 6f)
    }

    @Test
    fun shouldReportProgressForOnePoolWithRuntimeDiscovery() {
        val poolId = DevicePoolId("testpool")

        val test0 = MarathonTest("com.example", "SimpleTest", "method[0]", emptyList())
        val test1 = MarathonTest("com.example", "SimpleTest", "method[1]", emptyList())
        val test2 = MarathonTest("com.example", "SimpleTest", "method[2]", emptyList())

        reporter.testCountExpectation(poolId, 1)
        reporter.progress().shouldBeEqualTo(.0f)

        /**
         * [0] passed
         */
        reporter.testStarted(poolId, deviceInfo, test0)
        reporter.testPassed(poolId, deviceInfo, test0)
        reporter.progress().shouldBeEqualTo(1 / 1f)

        /**
         * [1] passed
         */
        reporter.testStarted(poolId, deviceInfo, test1)
        reporter.testPassed(poolId, deviceInfo, test1)
        reporter.progress().shouldBeEqualTo(2 / 1f)

        /**
         * [2] passed
         */
        reporter.testStarted(poolId, deviceInfo, test2)
        reporter.testPassed(poolId, deviceInfo, test2)
        reporter.progress().shouldBeEqualTo(3 / 1f)

        reporter.addTestDiscoveredDuringRuntime(poolId, test1)
        reporter.addTestDiscoveredDuringRuntime(poolId, test2)

        val progress = reporter.progress()
        progress.shouldBeEqualTo(6 / 6f)
    }
}
