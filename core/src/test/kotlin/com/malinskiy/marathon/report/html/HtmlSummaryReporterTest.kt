package com.malinskiy.marathon.report.html

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
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
import com.malinskiy.marathon.io.FileManager
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldHaveSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Instant
import com.malinskiy.marathon.test.Test as MarathonTest

class HtmlSummaryReporterTest {

    @TempDir
    lateinit var outputDir: File

    private val gson = Gson()

    private fun configuration(): Configuration = Configuration.Builder(
        name = "unit-test-run",
        outputDir = outputDir,
    ).apply {
        vendorConfiguration = VendorConfiguration.StubVendorConfiguration
        analyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics
        analyticsTracking = false
    }.build()

    private fun androidDevice(serial: String = "emulator-5554", api: String = "34") = DeviceInfo(
        operatingSystem = OperatingSystem(api),
        serialNumber = serial,
        model = "Pixel 6",
        manufacturer = "Google",
        networkState = NetworkState.CONNECTED,
        deviceFeatures = listOf(DeviceFeature.SCREENSHOT, DeviceFeature.VIDEO),
        healthy = true,
    )

    private fun iosDevice(serial: String = "SIM-1234", os: String = "17.0") = DeviceInfo(
        operatingSystem = OperatingSystem(os),
        serialNumber = serial,
        model = "iPhone 15",
        manufacturer = "Apple",
        networkState = NetworkState.CONNECTED,
        deviceFeatures = listOf(DeviceFeature.SCREENSHOT),
        healthy = true,
    )

    private fun testEvent(
        device: DeviceInfo,
        methodName: String,
        status: TestStatus,
        final: Boolean = true,
        startTime: Long = 1_000,
        endTime: Long = 1_500,
        batch: String = "batch-a",
    ) = TestEvent(
        Instant.now(),
        DevicePoolId("myPool"),
        device,
        TestResult(
            MarathonTest("com", "example", methodName, emptyList()),
            device,
            batch,
            status,
            startTime,
            endTime,
        ),
        final,
    )

    private fun reporter(): HtmlSummaryReporter =
        HtmlSummaryReporter(
            gson = gson,
            fileManager = FileManager(0, 255, outputDir),
            rootOutput = outputDir,
            configuration = configuration(),
        )

    private fun runReporter(report: ExecutionReport): JsonObject {
        reporter().generate(report)
        return extractPayload(File(outputDir, "html/index.html"), "mainData")
    }

    /**
     * The template embeds `window.<name> = <json>` on a single line inside a
     * `<script>` block; the line after it is a `//` comment. Isolate the JSON
     * by anchoring on the assignment and cutting at the newline.
     */
    private fun extractPayload(file: File, global: String): JsonObject {
        val text = file.readText()
        val afterAssign = text.substringAfter("window.$global = ")
        val jsonLine = afterAssign.substringBefore('\n').trim().trimEnd(';')
        return JsonParser.parseString(jsonLine).asJsonObject
    }

    @Test
    fun `emits schema version 2 with generated_at_ms`() {
        val device = androidDevice()
        val report = ExecutionReport(
            deviceConnectedEvents = listOf(DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)),
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceProviderPreparingEvent = emptyList(),
            testEvents = listOf(testEvent(device, "t1", TestStatus.PASSED)),
        )
        val json = runReporter(report)
        json.get("report_schema_version").asInt shouldBe 2
        json.get("generated_at_ms").asLong shouldBeGreater 0L
    }

    @Test
    fun `HtmlDevice uses snake_case with is_tablet and os details`() {
        val device = androidDevice(api = "34")
        val report = ExecutionReport(
            deviceConnectedEvents = listOf(DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)),
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceProviderPreparingEvent = emptyList(),
            testEvents = listOf(testEvent(device, "t1", TestStatus.PASSED)),
        )
        val json = runReporter(report)
        val htmlDevice = json.getAsJsonArray("pools").first().asJsonObject
            .getAsJsonArray("devices").first().asJsonObject
        htmlDevice.has("is_tablet") shouldBe true
        htmlDevice.has("isTable") shouldBe false
        htmlDevice.get("os_version").asString shouldBeEqualTo "34"
        htmlDevice.get("os_major").asInt shouldBeEqualTo 34
        htmlDevice.get("manufacturer").asString shouldBeEqualTo "Google"
        htmlDevice.get("model_name").asString shouldBeEqualTo "Pixel 6"
        htmlDevice.get("network_state").asString shouldBeEqualTo "CONNECTED"
        htmlDevice.getAsJsonArray("features")
            .map { it.asString } shouldContain "SCREENSHOT"
    }

    @Test
    fun `retry history is exposed as chronological attempts on the final test entry`() {
        val device = androidDevice()
        val report = ExecutionReport(
            deviceConnectedEvents = listOf(DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)),
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceProviderPreparingEvent = emptyList(),
            testEvents = listOf(
                testEvent(device, "flakyTest", TestStatus.FAILURE, final = false, startTime = 100, endTime = 200, batch = "b1"),
                testEvent(device, "flakyTest", TestStatus.FAILURE, final = false, startTime = 300, endTime = 400, batch = "b2"),
                testEvent(device, "flakyTest", TestStatus.PASSED, final = true, startTime = 500, endTime = 600, batch = "b3"),
            ),
        )
        reporter().generate(report)
        val fullTest = readFullTestFor("myPool", device.serialNumber, "com.example.flakyTest")
        val attempts = fullTest.getAsJsonArray("attempts")
        attempts shouldHaveSize 3
        attempts[0].asJsonObject.get("status").asString shouldBeEqualTo "failed"
        attempts[0].asJsonObject.get("attempt_index").asInt shouldBe 0
        attempts[0].asJsonObject.get("final").asBoolean shouldBe false
        attempts[2].asJsonObject.get("status").asString shouldBeEqualTo "passed"
        attempts[2].asJsonObject.get("final").asBoolean shouldBe true
        fullTest.get("attempt_count").asInt shouldBe 3
        fullTest.get("is_flaky").asBoolean shouldBe true
    }

    @Test
    fun `pool summary rolls up flaky_count and totalFlaky index count`() {
        val device = androidDevice()
        val report = ExecutionReport(
            deviceConnectedEvents = listOf(DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)),
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceProviderPreparingEvent = emptyList(),
            testEvents = listOf(
                testEvent(device, "flaky1", TestStatus.FAILURE, final = false, startTime = 10, endTime = 20),
                testEvent(device, "flaky1", TestStatus.PASSED, final = true, startTime = 30, endTime = 40),
                testEvent(device, "steady", TestStatus.PASSED, final = true, startTime = 50, endTime = 60),
            ),
        )
        val json = runReporter(report)
        json.get("total_flaky").asInt shouldBe 1
        json.getAsJsonArray("pools").first().asJsonObject
            .get("flaky_count").asInt shouldBe 1
    }

    @Test
    fun `attempts across different devices surface distinct_devices`() {
        val android = androidDevice("emulator-5554")
        val ios = iosDevice("SIM-1234")
        val report = ExecutionReport(
            deviceConnectedEvents = listOf(
                DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), android),
                DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), ios),
            ),
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceProviderPreparingEvent = emptyList(),
            testEvents = listOf(
                testEvent(android, "cross", TestStatus.FAILURE, final = false, startTime = 100, endTime = 200),
                testEvent(ios, "cross", TestStatus.PASSED, final = true, startTime = 300, endTime = 400),
            ),
        )
        reporter().generate(report)
        val fullTest = readFullTestFor("myPool", ios.serialNumber, "com.example.cross")
        val distinct = fullTest.getAsJsonArray("distinct_devices")
        distinct shouldHaveSize 2
        val serials = distinct.map { it.asJsonObject.get("serial").asString }.toSet()
        serials shouldBeEqualTo setOf("emulator-5554", "SIM-1234")
    }

    @Test
    fun `single-attempt test is not flaky`() {
        val device = androidDevice()
        val report = ExecutionReport(
            deviceConnectedEvents = listOf(DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)),
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceProviderPreparingEvent = emptyList(),
            testEvents = listOf(testEvent(device, "single", TestStatus.PASSED)),
        )
        reporter().generate(report)
        val fullTest = readFullTestFor("myPool", device.serialNumber, "com.example.single")
        fullTest.get("is_flaky").asBoolean shouldBe false
        fullTest.get("attempt_count").asInt shouldBe 1
    }

    @Test
    fun `log details page holds per-attempt entries`() {
        val device = androidDevice()
        val report = ExecutionReport(
            deviceConnectedEvents = listOf(DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)),
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceProviderPreparingEvent = emptyList(),
            testEvents = listOf(
                testEvent(device, "logged", TestStatus.FAILURE, final = false, startTime = 10, endTime = 20, batch = "b1"),
                testEvent(device, "logged", TestStatus.PASSED, final = true, startTime = 30, endTime = 40, batch = "b2"),
            ),
        )
        reporter().generate(report)
        val logs = extractPayload(
            File(outputDir, "html/pools/myPool/${device.serialNumber}/logs/com.example.logged.html"),
            "logs",
        )
        val attempts = logs.getAsJsonArray("attempts")
        attempts shouldHaveSize 2
        attempts[1].asJsonObject.get("final").asBoolean shouldBe true
    }

    @Test
    fun `emitted index does not contain the removed dollar-log template hook`() {
        val device = androidDevice()
        val report = ExecutionReport(
            deviceConnectedEvents = listOf(DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)),
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceProviderPreparingEvent = emptyList(),
            testEvents = listOf(testEvent(device, "t1", TestStatus.PASSED)),
        )
        reporter().generate(report)
        val emitted = File(outputDir, "html/index.html").readText()
        emitted.contains("\${log}") shouldBe false
    }

    private fun readFullTestFor(pool: String, deviceSerial: String, testId: String): JsonObject =
        extractPayload(File(outputDir, "html/pools/$pool/$deviceSerial/$testId.html"), "test")

    // Kluent doesn't ship a `shouldBeGreater` for Long; local infix keeps assertion sites readable.
    private infix fun Long.shouldBeGreater(other: Long): Long {
        require(this > other) { "Expected $this > $other" }
        return this
    }
}
