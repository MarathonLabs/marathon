package com.malinskiy.marathon.report.timeline

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.malinskiy.marathon.analytics.internal.sub.DeviceConnectedEvent
import com.malinskiy.marathon.analytics.internal.sub.DevicePreparingEvent
import com.malinskiy.marathon.analytics.internal.sub.DeviceProviderPreparingEvent
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
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.report.html.HtmlSummaryReporter
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldHaveSize
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Instant
import com.malinskiy.marathon.test.Test as MarathonTest

class TimelineSummaryProviderTest {

    @TempDir
    lateinit var outputDir: File

    private val deviceA = device("127.0.0.1:5037:localhost:1111")
    private val deviceB = device("127.0.0.1:5038:localhost:2222")
    private val deviceC = device("127.0.0.1:5039:localhost:3333")

    private fun device(serial: String) = DeviceInfo(
        operatingSystem = OperatingSystem("37"),
        serialNumber = serial,
        model = "sdk_gphone16k_x86_64",
        manufacturer = "Google",
        networkState = NetworkState.CONNECTED,
        deviceFeatures = listOf(DeviceFeature.SCREENSHOT),
        healthy = true,
    )

    private fun testEvent(
        device: DeviceInfo,
        method: String,
        status: TestStatus,
        startTime: Long,
        final: Boolean = false,
        clazz: String = "Suite",
        pool: String = "omni",
    ) = TestEvent(
        Instant.ofEpochMilli(startTime),
        DevicePoolId(pool),
        device,
        TestResult(MarathonTest("com.example", clazz, method, emptyList()), device, "batch-$startTime", status, startTime, startTime + 50),
        final,
    )

    private fun report(
        vararg events: TestEvent,
        devices: List<DeviceInfo> = listOf(deviceA, deviceB, deviceC),
        preparing: List<DevicePreparingEvent> = emptyList(),
        providerInit: List<DeviceProviderPreparingEvent> = emptyList(),
    ) = ExecutionReport(
        deviceConnectedEvents = devices.map { DeviceConnectedEvent(Instant.EPOCH, DevicePoolId("omni"), it) },
        deviceDisconnectedEvents = emptyList(),
        devicePreparingEvents = preparing,
        deviceProviderPreparingEvent = providerInit,
        testEvents = events.toList(),
    )

    private fun TimelineExecutionResult.bars(testName: String): List<Data> =
        measures.flatMap { it.data }.filter { it.testName == testName }.sortedBy { it.startDate }

    @Test
    fun `retries on different devices get sequential attempt indices across the whole run`() {
        // Events deliberately out of chronological order and spread across devices.
        val result = TimelineSummaryProvider().generate(
            report(
                testEvent(deviceC, "flaky", TestStatus.PASSED, startTime = 300, final = true),
                testEvent(deviceA, "flaky", TestStatus.FAILURE, startTime = 100),
                testEvent(deviceB, "flaky", TestStatus.FAILURE, startTime = 200),
            ),
        )
        val bars = result.bars("Suite.flaky")
        bars shouldHaveSize 3
        bars.map { it.attemptIndex } shouldBeEqualTo listOf(0, 1, 2)
        bars.map { it.metricType } shouldBeEqualTo listOf(MetricType.FAILURE, MetricType.FAILURE, MetricType.PASSED)
    }

    @Test
    fun `retries on the same device also get sequential attempt indices`() {
        val result = TimelineSummaryProvider().generate(
            report(
                testEvent(deviceA, "flaky", TestStatus.FAILURE, startTime = 100),
                testEvent(deviceA, "flaky", TestStatus.FAILURE, startTime = 200),
                testEvent(deviceB, "flaky", TestStatus.PASSED, startTime = 300, final = true),
                testEvent(deviceA, "flaky", TestStatus.FAILURE, startTime = 150),
            ),
        )
        result.bars("Suite.flaky").map { it.attemptIndex } shouldBeEqualTo listOf(0, 1, 2, 3)
    }

    @Test
    fun `attempt numbering is independent per pool`() {
        val result = TimelineSummaryProvider().generate(
            report(
                testEvent(deviceA, "shared", TestStatus.FAILURE, startTime = 100, pool = "p1"),
                testEvent(deviceB, "shared", TestStatus.PASSED, startTime = 200, pool = "p2", final = true),
                testEvent(deviceC, "shared", TestStatus.PASSED, startTime = 300, pool = "p1", final = true),
            ),
        )
        val byPool = result.bars("Suite.shared").groupBy({ it.poolId }, { it.attemptIndex })
        byPool shouldBeEqualTo mapOf("p1" to listOf(0, 1), "p2" to listOf(0))
    }

    @Test
    fun `attempt numbering is independent per class for the same method name`() {
        val result = TimelineSummaryProvider().generate(
            report(
                testEvent(deviceA, "run", TestStatus.PASSED, startTime = 100, clazz = "First", final = true),
                testEvent(deviceB, "run", TestStatus.PASSED, startTime = 200, clazz = "Second", final = true),
            ),
        )
        result.bars("First.run").single().attemptIndex shouldBeEqualTo 0
        result.bars("Second.run").single().attemptIndex shouldBeEqualTo 0
    }

    @Test
    fun `every attempt bar links to the final attempt's device and shares one filename`() {
        val result = TimelineSummaryProvider().generate(
            report(
                testEvent(deviceA, "flaky", TestStatus.FAILURE, startTime = 100),
                testEvent(deviceB, "flaky", TestStatus.PASSED, startTime = 200, final = true),
            ),
        )
        val bars = result.bars("Suite.flaky")
        bars.map { it.deviceSerial }.toSet() shouldBeEqualTo setOf(deviceB.safeSerialNumber)
        bars.map { it.testFilename }.toSet() shouldBeEqualTo setOf("com.example.Suite.flaky.html")
        bars.map { it.poolId }.toSet() shouldBeEqualTo setOf("omni")
    }

    @Test
    fun `each attempt bar is drawn on the row of the device it ran on`() {
        val result = TimelineSummaryProvider().generate(
            report(
                testEvent(deviceA, "flaky", TestStatus.FAILURE, startTime = 100),
                testEvent(deviceB, "flaky", TestStatus.PASSED, startTime = 200, final = true),
            ),
        )
        val rowOf = result.measures.associate { m -> m.measure to m.data.map { it.startDate } }
        rowOf[deviceA.serialNumber] shouldBeEqualTo listOf(100L)
        rowOf[deviceB.serialNumber] shouldBeEqualTo listOf(200L)
    }

    @Test
    fun `device prepare and provider init bars carry no test identity`() {
        // The report UI keys its attempt highlight on testFilename; non-test
        // bars must leave it null so they never join a test's highlight group.
        val result = TimelineSummaryProvider().generate(
            report(
                testEvent(deviceA, "t", TestStatus.PASSED, startTime = 500, final = true),
                devices = listOf(deviceA),
                preparing = listOf(DevicePreparingEvent(Instant.ofEpochMilli(10), Instant.ofEpochMilli(20), deviceA.serialNumber)),
                providerInit = listOf(DeviceProviderPreparingEvent(Instant.ofEpochMilli(1), Instant.ofEpochMilli(2), deviceA.serialNumber)),
            ),
        )
        val nonTest = result.measures.flatMap { it.data }.filter { it.metricType in setOf(MetricType.DEVICE_PREPARE, MetricType.DEVICE_PROVIDER_INIT) }
        nonTest shouldHaveSize 2
        nonTest.forEach {
            it.testFilename.shouldBeNull()
            it.attemptIndex.shouldBeNull()
            it.poolId.shouldBeNull()
        }
    }

    /**
     * Contract between the two reporters: clicking a timeline bar opens
     * `pools/<poolId>/<deviceSerial>/<testFilename>#/attempt/<attemptIndex>`,
     * so that file must exist and its attempt N must be the bar's attempt.
     */
    @Test
    fun `timeline bars resolve to the matching attempt on the emitted test page`() {
        val report = report(
            testEvent(deviceC, "flaky", TestStatus.PASSED, startTime = 300, final = true),
            testEvent(deviceA, "flaky", TestStatus.FAILURE, startTime = 100),
            testEvent(deviceB, "flaky", TestStatus.FAILURE, startTime = 200),
            testEvent(deviceA, "stable", TestStatus.PASSED, startTime = 400, final = true),
        )
        val configuration = Configuration.Builder(name = "contract", outputDir = outputDir).apply {
            vendorConfiguration = VendorConfiguration.StubVendorConfiguration
            analyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics
            analyticsTracking = false
        }.build()
        HtmlSummaryReporter(Gson(), FileManager(0, 255, outputDir), outputDir, configuration).generate(report)

        val htmlDir = File(outputDir, "html")
        val timeline = payload(File(htmlDir, "index.html"), "timeline")
        val bars = timeline.getAsJsonArray("measures")
            .flatMap { it.asJsonObject.getAsJsonArray("data") }
            .map { it.asJsonObject }
            .filter { it.has("testFilename") }
        bars shouldHaveSize 4

        bars.forEach { bar ->
            val page = File(htmlDir, "pools/${bar["poolId"].asString}/${bar["deviceSerial"].asString}/${bar["testFilename"].asString}")
            page.exists() shouldBe true
            val attempts = payload(page, "test").getAsJsonArray("attempts")
            val attempt = attempts[bar["attemptIndex"].asInt].asJsonObject.shouldNotBeNull()
            attempt["attempt_index"].asInt shouldBeEqualTo bar["attemptIndex"].asInt
            attempt["start_time_ms"].asLong shouldBeEqualTo bar["startDate"].asLong
        }
    }

    private fun payload(file: File, global: String): JsonObject {
        val json = file.readText().substringAfter("window.$global = ").substringBefore('\n').trim().trimEnd(';')
        return JsonParser.parseString(json).asJsonObject
    }
}
