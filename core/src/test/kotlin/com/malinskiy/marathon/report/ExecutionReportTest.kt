package com.malinskiy.marathon.report

import com.malinskiy.marathon.analytics.internal.sub.DeviceConnectedEvent
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Instant
import com.malinskiy.marathon.test.Test as MarathonTest


class ExecutionReportTest {
    val configuration = Configuration.Builder(
        name = "",
        outputDir = File("src/test/resources/output/"),
    ).apply {
        vendorConfiguration = VendorConfiguration.StubVendorConfiguration
        analyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics
        analyticsTracking = false
    }.build()

    private val reportWithoutRetries: ExecutionReport by lazy {
        val device = DeviceInfo(
            operatingSystem = OperatingSystem("23"),
            serialNumber = "xxyyzz",
            model = "Android SDK built for x86",
            manufacturer = "unknown",
            networkState = NetworkState.CONNECTED,
            deviceFeatures = listOf(DeviceFeature.SCREENSHOT, DeviceFeature.VIDEO),
            healthy = true
        )
        ExecutionReport(
            deviceConnectedEvents = listOf(
                DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)
            ),
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceProviderPreparingEvent = emptyList(),
            testEvents = listOf(
                createTestEvent(device, "test1", TestStatus.INCOMPLETE),
                createTestEvent(device, "test2", TestStatus.PASSED),
                createTestEvent(device, "test3", TestStatus.FAILURE)
            )
        )
    }

    private val reportWithRetries: ExecutionReport by lazy {
        val device = DeviceInfo(
            operatingSystem = OperatingSystem("23"),
            serialNumber = "xxyyzz",
            model = "Android SDK built for x86",
            manufacturer = "unknown",
            networkState = NetworkState.CONNECTED,
            deviceFeatures = listOf(DeviceFeature.SCREENSHOT, DeviceFeature.VIDEO),
            healthy = true
        )
        ExecutionReport(
            deviceConnectedEvents = listOf(
                DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)
            ),
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceProviderPreparingEvent = emptyList(),
            testEvents = listOf(
                createTestEvent(device, "test2", TestStatus.FAILURE, false),
                createTestEvent(device, "test2", TestStatus.FAILURE, false),
                createTestEvent(device, "test2", TestStatus.PASSED, true),
                createTestEvent(device, "test3", TestStatus.FAILURE, false),
                createTestEvent(device, "test3", TestStatus.FAILURE, false),
                createTestEvent(device, "test3", TestStatus.FAILURE, true)
            )
        )
    }

    private fun createTestEvent(deviceInfo: DeviceInfo, methodName: String, status: TestStatus, final: Boolean = true): TestEvent {
        return TestEvent(
            Instant.now(),
            DevicePoolId("myPool"),
            deviceInfo,
            TestResult(
                MarathonTest("com", "example", methodName, emptyList()),
                deviceInfo,
                "stub-batch",
                status,
                0,
                100
            ),
            final
        )
    }

    @Test
    fun `without retries should not include the INCOMPLETE test`() {
        val summary = reportWithoutRetries.summary
        val tests = summary.pools.flatMap { it.tests }
        tests.filter { it.status == TestStatus.INCOMPLETE }.count() shouldBe 0
    }

    @Test
    fun `without retries should include 1 PASSED test`() {
        val summary = reportWithoutRetries.summary
        val tests = summary.pools.flatMap { it.tests }
        tests.filter { it.status == TestStatus.PASSED }.count() shouldBe 1
    }

    @Test
    fun `without retries should include 1 FAILED test`() {
        val summary = reportWithoutRetries.summary
        val tests = summary.pools.flatMap { it.tests }
        tests.filter { it.status == TestStatus.FAILURE }.count() shouldBe 1
    }

    @Test
    fun `with retries should include only one instance of test2 and it's PASSED`() {
        val summary = reportWithRetries.summary
        val tests = summary.pools.flatMap { it.tests }.filter { it.test.method == "test2" }
        tests.size shouldBe 1
        tests.first().status shouldBe TestStatus.PASSED
    }

    @Test
    fun `with retries should include only one instance of test3 and it's FAILED`() {
        val summary = reportWithRetries.summary
        val tests = summary.pools.flatMap { it.tests }.filter { it.test.method == "test3" }
        tests.size shouldBe 1
        tests.first().status shouldBe TestStatus.FAILURE
    }
}
