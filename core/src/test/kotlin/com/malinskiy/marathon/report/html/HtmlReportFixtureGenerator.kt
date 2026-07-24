package com.malinskiy.marathon.report.html

import com.google.gson.Gson
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
import com.malinskiy.marathon.execution.Attachment
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.report.timeline.TimelineSummaryProvider
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Instant
import kotlin.random.Random
import com.malinskiy.marathon.test.Test as MarathonTest

/**
 * Produces a preview of the HTML report by running the *real* Kotlin reporter
 * pipeline against a synthetic [ExecutionReport]. Emits under
 * `<module>/build/fixtures/{android,ios}/html/` so a human can double-click
 * `index.html` and see the current UI end-to-end.
 *
 * This intentionally lives in `src/test` and is driven as a JUnit test so
 * Gradle can run it directly (`./gradlew :core:test --tests "*Fixture*"`).
 * The Gradle wrapper task `generateHtmlReportFixture` targets exactly this
 * class name, keeping the fixture output tied to the real emitted shape —
 * no JS/TS reimplementation to drift.
 *
 * Deterministic via a seeded [Random] so screenshots + smoke tests don't
 * churn across runs.
 */
class HtmlReportFixtureGenerator {

    @Test
    fun generateAndroidFixture() {
        val target = fixturesRoot("android")
        emit(
            configurationName = "Android UI regression",
            outputDir = target,
            executionReport = androidReport(),
        )
        println("Android fixture: file://${target.absolutePath}/html/index.html")
    }

    @Test
    fun generateIosFixture() {
        val target = fixturesRoot("ios")
        emit(
            configurationName = "iOS UI regression",
            outputDir = target,
            executionReport = iosReport(),
        )
        println("iOS fixture: file://${target.absolutePath}/html/index.html")
    }

    /**
     * Location: `<module>/build/fixtures/<platform>/`. Wiped fresh on each
     * run — Gradle caches the JUnit invocation but we always want a clean
     * tree because pool/test filenames are deterministic and orphan files
     * would confuse the smoke check.
     */
    private fun fixturesRoot(platform: String): File {
        val moduleBuildDir = File(System.getProperty("user.dir"), "build")
        return File(moduleBuildDir, "fixtures/$platform").apply {
            deleteRecursively()
            mkdirs()
        }
    }

    private fun emit(configurationName: String, outputDir: File, executionReport: ExecutionReport) {
        val configuration = Configuration.Builder(name = configurationName, outputDir = outputDir).apply {
            vendorConfiguration = VendorConfiguration.StubVendorConfiguration
            analyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics
            analyticsTracking = false
        }.build()
        val gson = Gson()
        val fileManager = FileManager(0, 255, outputDir)

        // Persist synthetic log files under the paths the reporter expects,
        // so `readInlineLogBody` finds real bytes and `log_body` gets inlined
        // just like production.
        writeSyntheticLogs(fileManager, executionReport, androidStyle = configurationName.contains("Android"))
        writeSyntheticScreenshots(fileManager, executionReport)
        val reportWithVideo = attachVideoStubs(fileManager, executionReport)

        HtmlSummaryReporter(gson, fileManager, outputDir, configuration, TimelineSummaryProvider())
            .generate(reportWithVideo)
    }

    private fun writeSyntheticLogs(
        fileManager: FileManager,
        report: ExecutionReport,
        androidStyle: Boolean,
    ) {
        val seen = HashSet<File>()
        for (event in report.testEvents) {
            val r = event.testResult
            val file = fileManager.createFile(FileType.LOG, event.poolId, r.device, r.test, r.testBatchId)
            file.parentFile.mkdirs()
            if (!seen.add(file)) continue
            val seed = (r.startTime xor r.test.method.hashCode().toLong()).toInt()
            val body = if (androidStyle) fakeLogcat(seed, r.status == TestStatus.FAILURE)
            else fakeOsLog(seed, r.status == TestStatus.FAILURE)
            file.writeText(body)
        }
    }

    /**
     * Paint a synthetic screenshot on disk for every attempt on a device
     * that reports SCREENSHOT capability. Reporter's `receiveScreenshotPath`
     * checks `file.exists()` before wiring the screenshot into the payload,
     * so writing real bytes here is what makes them appear on TestPage.
     *
     * Rendered as a PNG (400x300) with the attempt id + status baked in as
     * text so a human previewing the fixture can eyeball whether the correct
     * screenshot lands on the correct card. Written under the same `.gif`
     * path convention the reporter uses (`FileType.SCREENSHOT`); browsers
     * mime-sniff the actual bytes when the `<img>` renders it.
     */
    private fun writeSyntheticScreenshots(fileManager: FileManager, report: ExecutionReport) {
        val seen = HashSet<File>()
        for (event in report.testEvents) {
            val r = event.testResult
            if (!r.device.deviceFeatures.contains(DeviceFeature.SCREENSHOT)) continue
            val file = fileManager.createFile(
                FileType.SCREENSHOT, event.poolId, r.device, r.test, r.testBatchId,
            )
            file.parentFile.mkdirs()
            if (!seen.add(file)) continue
            writePngLabelled(file, "${r.test.clazz}.${r.test.method}", r.status)
        }
    }

    /**
     * For every failed attempt on a video-capable device, copy the sample
     * mp4 into place and thread it through as an `Attachment` on the
     * `TestResult`. Reporter reads `attachments.filter(type == VIDEO)` and
     * turns matching entries into `<video>` sources on TestPage.
     *
     * Sample source: `html-report/dev-assets/sample.mp4`. Missing sample is
     * a no-op — fixture still generates cleanly with an empty videos array.
     * To exercise the video path, drop any real mp4 at that location and
     * regenerate; the copy runs at test time, no rebuild needed.
     */
    private fun attachVideoStubs(fileManager: FileManager, report: ExecutionReport): ExecutionReport {
        val sample = File(System.getProperty("user.dir")).resolveSibling("html-report/dev-assets/sample.mp4")
        if (!sample.exists()) return report
        val newTestEvents = report.testEvents.map { event ->
            val r = event.testResult
            val wantsVideo = r.device.deviceFeatures.contains(DeviceFeature.VIDEO) &&
                r.status == TestStatus.FAILURE
            if (!wantsVideo) return@map event
            val videoFile = fileManager.createFile(
                FileType.VIDEO, event.poolId, r.device, r.test, r.testBatchId,
            )
            videoFile.parentFile.mkdirs()
            if (!videoFile.exists()) sample.copyTo(videoFile, overwrite = true)
            val updated = r.copy(
                attachments = r.attachments + Attachment(videoFile, AttachmentType.VIDEO, "video"),
            )
            event.copy(testResult = updated)
        }
        return report.copy(testEvents = newTestEvents)
    }

    /**
     * Paint a labelled screenshot placeholder — colored per status, with
     * the class.method text at the top so previewing the fixture makes it
     * obvious which shot goes with which attempt.
     */
    private fun writePngLabelled(target: File, label: String, status: TestStatus) {
        val width = 400
        val height = 300
        val img = java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        try {
            g.setRenderingHint(
                java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON,
            )
            val (bg, stripe) = paletteFor(status)
            g.color = bg
            g.fillRect(0, 0, width, height)
            g.color = stripe
            g.fillRect(0, 0, width, 32)
            // Faux UI noise so the placeholder doesn't look like a solid block.
            val rng = Random(label.hashCode())
            g.color = java.awt.Color(255, 255, 255, 32)
            repeat(20) {
                val x = rng.nextInt(width)
                val y = 40 + rng.nextInt(height - 60)
                val w = 20 + rng.nextInt(90)
                val h = 8 + rng.nextInt(28)
                g.fillRect(x, y, w, h)
            }
            g.color = java.awt.Color(248, 250, 252)
            g.font = java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12)
            g.drawString(label.take(60), 10, 21)
            g.color = java.awt.Color(148, 163, 184)
            g.font = java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 10)
            g.drawString("fixture screenshot · ${status.name.lowercase()}", 10, height - 10)
        } finally {
            g.dispose()
        }
        // Write PNG bytes even though the extension is `.gif` (Kotlin's
        // FileType.SCREENSHOT). Browsers mime-sniff the actual bytes for
        // `<img>`, so this renders correctly under `file://`.
        javax.imageio.ImageIO.write(img, "png", target)
    }

    private fun paletteFor(status: TestStatus): Pair<java.awt.Color, java.awt.Color> = when (status) {
        TestStatus.FAILURE -> java.awt.Color(0x7f, 0x1d, 0x1d) to java.awt.Color(0xdc, 0x26, 0x26)
        TestStatus.IGNORED, TestStatus.ASSUMPTION_FAILURE ->
            java.awt.Color(0x78, 0x35, 0x0f) to java.awt.Color(0xea, 0xb3, 0x08)
        else -> java.awt.Color(0x13, 0x4e, 0x4a) to java.awt.Color(0x22, 0xc5, 0x5e)
    }

    // ---------- Report builders -------------------------------------------------

    private fun androidReport(): ExecutionReport = syntheticReport(
        seed = 0xA11CE,
        poolPrefix = "pool",
        deviceCatalog = ANDROID_DEVICES,
    )

    private fun iosReport(): ExecutionReport = syntheticReport(
        seed = 0xF00D,
        poolPrefix = "sim-pool",
        deviceCatalog = IOS_DEVICES,
    )

    /**
     * Fixture size knobs — override at the CLI via
     * `./gradlew :core:generateHtmlReportFixture -PfixturePools=6 -PfixtureTests=5000`.
     * Total test count is split evenly across pools; the device catalog cycles
     * to fill each pool with 3–10 devices proportional to the total.
     *
     * Defaults keep the deterministic 2-pool × 125-test shape used by earlier
     * screenshots + smoke checks so nothing surprises when a knob isn't set.
     */
    private data class FixtureScale(val poolCount: Int, val totalTests: Int, val devicesPerPool: Int) {
        fun testsPerPool(): Int = (totalTests + poolCount - 1) / poolCount
    }

    private fun readScale(): FixtureScale {
        val pools = System.getProperty("fixturePools")?.toIntOrNull()?.coerceAtLeast(1) ?: DEFAULT_POOLS
        val totalTests = System.getProperty("fixtureTests")?.toIntOrNull()?.coerceAtLeast(1) ?: DEFAULT_TOTAL_TESTS
        // Rough sizing: more tests deserve more devices per pool up to the
        // catalog cap (20). Every extra 200 tests unlocks another device.
        val perPool = System.getProperty("fixtureDevicesPerPool")?.toIntOrNull()
            ?: (3 + totalTests / 200).coerceIn(3, DEVICE_CATALOG_MAX)
        return FixtureScale(pools, totalTests, perPool)
    }

    /**
     * Fabricate an [ExecutionReport] whose timeline layout mirrors a real
     * marathon run.
     *
     * Modeled behavior:
     * - **Staggered device onboarding.** Half the devices per pool come online
     *   at t=0. The other half stagger in over the next 5 minutes.
     * - **Per-device init/prepare bars.** Every device gets a
     *   `DeviceProviderPreparingEvent` (10–30 s) followed by a
     *   `DevicePreparingEvent` (10–30 s) before its first test.
     * - **Wider test-duration distribution.** ~65% "normal" (10–90 s), ~20%
     *   "short" (0.2–5 s), ~15% "long-tail" (60–180 s). No 8.5 s bucketing.
     * - **LPT scheduling.** Tests sorted descending by nominal duration and
     *   round-robin assigned to per-device queues, so long tests kick off
     *   first and long queues finish around the same time as short ones.
     * - **Tight inter-test gap.** ~10 s (jitter 6–14 s) between tests on a
     *   given device, matching what marathon does with batching / no-batching.
     * - **Retries land on the same device by default** (occasionally hopping
     *   to another) and queue immediately after the failing attempt.
     */
    private fun syntheticReport(seed: Int, poolPrefix: String, deviceCatalog: List<DeviceInfo>): ExecutionReport {
        val rng = Random(seed)
        val scale = readScale()
        val pools = List(scale.poolCount) { "$poolPrefix-${it + 1}" }
        // Build each pool by cycling through the catalog with a per-pool
        // offset so pools don't share the exact same device serials. Cap at
        // catalog size to avoid duplicate serials within one pool.
        val perDevice = scale.devicesPerPool.coerceAtMost(deviceCatalog.size)
        val perPoolDevices = List(scale.poolCount) { poolIdx ->
            List(perDevice) { i -> deviceCatalog[(poolIdx * 7 + i) % deviceCatalog.size] }
        }
        val testsPerPool = scale.testsPerPool()
        println(
            "Fixture scale: ${scale.poolCount} pools × ${testsPerPool} tests × " +
                "$perDevice devices (~${scale.poolCount * testsPerPool} tests total)"
        )

        val connectedEvents = mutableListOf<DeviceConnectedEvent>()
        val providerEvents = mutableListOf<DeviceProviderPreparingEvent>()
        val prepareEvents = mutableListOf<DevicePreparingEvent>()
        val testEvents = mutableListOf<TestEvent>()

        pools.forEachIndexed { poolIdx, poolName ->
            val poolId = DevicePoolId(poolName)
            val poolDevices = perPoolDevices[poolIdx]

            // 1. Stagger device onboarding: half at t=0, rest evenly spread
            //    across the next STAGGER_WINDOW_MS. Deterministic index-based
            //    stagger (not random) keeps the fixture reproducible.
            val readyAt = LinkedHashMap<DeviceInfo, Long>()
            val halfOnline = (poolDevices.size + 1) / 2
            poolDevices.forEachIndexed { i, device ->
                val connectMs = if (i < halfOnline) BASE_TIME_MS
                else BASE_TIME_MS + (STAGGER_WINDOW_MS * (i - halfOnline + 1)) / (poolDevices.size - halfOnline + 1)
                connectedEvents += DeviceConnectedEvent(Instant.ofEpochMilli(connectMs), poolId, device)

                // Provider init then device prepare — each 10–30 s per marathon
                // vendor conventions.
                val providerStart = connectMs
                val providerEnd = providerStart + 10_000L + rng.nextInt(20_000)
                providerEvents += DeviceProviderPreparingEvent(
                    Instant.ofEpochMilli(providerStart), Instant.ofEpochMilli(providerEnd), device.serialNumber,
                )
                val prepareStart = providerEnd + rng.nextInt(500)
                val prepareEnd = prepareStart + 10_000L + rng.nextInt(20_000)
                prepareEvents += DevicePreparingEvent(
                    Instant.ofEpochMilli(prepareStart), Instant.ofEpochMilli(prepareEnd), device.serialNumber,
                )
                readyAt[device] = prepareEnd
            }

            // 2. Fabricate test metadata + nominal duration for every test.
            //    Duration distribution is intentionally heavier-tailed than the
            //    previous uniform 200 ms–8.5 s spread.
            data class TestSpec(
                val marathonTest: MarathonTest,
                val simpleClass: String,
                val method: String,
                val nominalDurationMs: Long,
                val finalStatus: TestStatus,
                val statusesInOrder: List<TestStatus>,
            )

            val specs = List(testsPerPool) { testIdx ->
                val classFqn = TEST_CLASSES.random(rng)
                val method = "${METHODS.random(rng)}-$testIdx"
                val pkg = classFqn.substringBeforeLast('.', missingDelimiterValue = "com.example")
                val simpleClass = classFqn.substringAfterLast('.')
                val marathonTest = MarathonTest(pkg, simpleClass, method, emptyList())

                val nominal = weightedDurationMs(rng)
                val finalStatus = weightedStatus(rng)
                val extraAttempts = when {
                    finalStatus == TestStatus.FAILURE && rng.nextFloat() < 0.55f -> 1 + rng.nextInt(2)
                    finalStatus == TestStatus.PASSED && rng.nextFloat() < 0.18f -> 1 + rng.nextInt(2)
                    else -> 0
                }
                val statuses = buildList<TestStatus> {
                    repeat(extraAttempts) { add(TestStatus.FAILURE) }
                    add(finalStatus)
                }
                val statusesFinal = if (statuses.size > 1 && statuses.last() == TestStatus.PASSED && rng.nextFloat() < 0.5f) {
                    val idx = rng.nextInt(0, statuses.size - 1)
                    statuses.toMutableList().apply { set(idx, TestStatus.PASSED) }
                } else statuses

                TestSpec(marathonTest, simpleClass, method, nominal, finalStatus, statusesFinal)
            }

            // 3. LPT scheduling — sort descending by nominal duration, then
            //    dispatch to the device queue with the earliest current cursor.
            //    This is roughly what marathon's `SortingStrategy = "durations"`
            //    + basic per-device scheduling produces at runtime.
            val queueCursor = HashMap(readyAt.mapValues { it.value })
            for (spec in specs.sortedByDescending { it.nominalDurationMs }) {
                val hostDevice = queueCursor.minBy { it.value }.key

                spec.statusesInOrder.forEachIndexed { attemptIdx, status ->
                    // Attempts occasionally hop to a different device (~15%),
                    // mirroring marathon's retry policy of preferring a fresh
                    // shard when available. Otherwise the retry runs on the
                    // same device right after the failure.
                    val device = if (attemptIdx > 0 && rng.nextFloat() < 0.15f)
                        poolDevices.random(rng) else hostDevice
                    val cursor = queueCursor.getValue(device)
                    val start = cursor
                    // Add mild per-attempt jitter (±25%) around the nominal
                    // so retries aren't visually identical bars.
                    val jitter = (spec.nominalDurationMs * (0.75 + rng.nextDouble(0.5))).toLong()
                    val end = start + jitter.coerceAtLeast(200)
                    val batchId = "batch-%02d-%08x".format(attemptIdx + 1, rng.nextInt())
                    testEvents += TestEvent(
                        instant = Instant.ofEpochMilli(end),
                        poolId = poolId,
                        device = device,
                        testResult = TestResult(
                            test = spec.marathonTest,
                            device = device,
                            testBatchId = batchId,
                            status = status,
                            startTime = start,
                            endTime = end,
                            stacktrace = if (status == TestStatus.FAILURE)
                                fakeStacktrace(rng, spec.simpleClass, spec.method) else null,
                        ),
                        final = attemptIdx == spec.statusesInOrder.lastIndex,
                    )
                    queueCursor[device] = end + INTER_TEST_GAP_MS + rng.nextLong(-4_000L, 4_000L)
                }
            }
        }

        return ExecutionReport(
            deviceConnectedEvents = connectedEvents,
            deviceDisconnectedEvents = emptyList(),
            devicePreparingEvents = prepareEvents,
            deviceProviderPreparingEvent = providerEvents,
            testEvents = testEvents,
        )
    }

    /**
     * Weighted duration sampler shaped to match what a real UI suite emits:
     * ~20% quick smoke tests, ~65% normal-length flow tests, ~15% long-tail
     * end-to-end scenarios that push toward 3 minutes.
     */
    private fun weightedDurationMs(rng: Random): Long {
        val roll = rng.nextInt(100)
        return when {
            roll < 20 -> 200L + rng.nextInt(4_800)              // 0.2 s – 5 s
            roll < 85 -> 10_000L + rng.nextInt(80_000)          // 10 s – 90 s
            else -> 60_000L + rng.nextInt(120_000)              // 60 s – 180 s
        }
    }

    private fun weightedStatus(rng: Random): TestStatus {
        val roll = rng.nextInt(100)
        return when {
            roll < 78 -> TestStatus.PASSED
            roll < 93 -> TestStatus.FAILURE
            else -> TestStatus.IGNORED
        }
    }

    private fun fakeStacktrace(rng: Random, simpleClass: String, method: String): String {
        val line = 20 + rng.nextInt(300)
        return """
            org.junit.ComparisonFailure: expected:<[Alice]> but was:<[Bob]>
            	at com.example.$simpleClass.$method(${simpleClass}.kt:$line)
            	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
            	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
            	at java.base/java.lang.reflect.Method.invoke(Method.java:568)
            	at org.junit.runners.model.FrameworkMethod${'$'}1.runReflectiveCall(FrameworkMethod.java:59)
        """.trimIndent()
    }

    // ---------- Synthetic log bodies -------------------------------------------

    private fun fakeLogcat(seed: Int, includeCrash: Boolean): String {
        val rng = Random(seed)
        val tags = listOf("ActivityManager", "MotionEvent", "OkHttp", "System.err", "Timeline", "View", "PackageManager", "InstrumentationTestRunner")
        val levels = listOf("V", "D", "I", "W", "E")
        val messages = listOf(
            "Frame drawn in 8.2ms; skipped_frames=0",
            "Starting activity com.example.app.MainActivity",
            "ConnectionPool releasing idle connection to https://api.example.com",
            "Task loaded into memory; touch=false, scroll=0",
            "IOException: unable to reach https://staging.example.com/v1/users",
            "Unhandled exception in coroutine scope; job cancelled",
        )
        val lines = 80 + rng.nextInt(180)
        val pid = 1000 + rng.nextInt(32000)
        val builder = StringBuilder()
        for (i in 0 until lines) {
            val level = levels.random(rng)
            val tag = tags.random(rng)
            val message = messages.random(rng)
            val minute = 10 + (i / 60) % 50
            val second = i % 60
            val millis = rng.nextInt(1000)
            builder.appendLine(
                "06-14 %02d:%02d:%02d.%03d  %5d  %5d %s %s: %s"
                    .format(9 + rng.nextInt(12), minute, second, millis, pid, pid + rng.nextInt(20), level, tag, message)
            )
        }
        if (includeCrash) {
            builder.appendLine("06-14 10:12:03.456  %5d  %5d E AndroidRuntime: FATAL EXCEPTION: main".format(pid, pid))
            builder.appendLine("06-14 10:12:03.456  %5d  %5d E AndroidRuntime: java.lang.NullPointerException: attempt to invoke virtual method 'java.lang.String com.example.User.getName()' on a null object reference".format(pid, pid))
            builder.appendLine("06-14 10:12:03.456  %5d  %5d E AndroidRuntime:     at com.example.app.SettingsActivity.onCreate(SettingsActivity.kt:42)".format(pid, pid))
        }
        return builder.toString()
    }

    private fun fakeOsLog(seed: Int, includeCrash: Boolean): String {
        val rng = Random(seed)
        val subsystems = listOf(
            "com.apple.CoreMedia", "com.apple.WebKit", "com.apple.network",
            "com.example.app", "com.example.app.push", "com.example.app.database",
        )
        val levels = listOf("Default", "Info", "Debug", "Error", "Fault", "Notice")
        val messages = listOf(
            "launching XPC service with mach_service=com.example.app.push",
            "WebSocket handshake complete; peer=fe80::abcd:1234",
            "CoreMedia decoder started for AVC-1080p60 (h264)",
            "network path evaluation: satisfied via en0 (Wi-Fi)",
            "app entered background; suspending audio session",
            "SQL statement failed: no such table: cached_users",
        )
        val lines = 60 + rng.nextInt(160)
        val pid = 200 + rng.nextInt(900)
        val builder = StringBuilder()
        for (i in 0 until lines) {
            val level = levels.random(rng)
            val subsystem = subsystems.random(rng)
            val message = messages.random(rng)
            val hour = 9 + rng.nextInt(8)
            val minute = i / 60 % 60
            val second = i % 60
            val micros = "%06d".format(rng.nextInt(999_999))
            val activity = "0x%x".format(0xA0000 + i)
            builder.appendLine(
                "2026-06-14 %02d:%02d:%02d.%s-0700  0x%x     %-9s %s            %d     0    %s: %s"
                    .format(hour, minute, second, micros, 0x1A0 + (i % 8), level, activity, pid, subsystem, message)
            )
        }
        if (includeCrash) {
            builder.appendLine("2026-06-14 10:12:03.456789-0700  0x1e2     Fault     0xb0001            %d     0    com.example.app: assertion failed: precondition value != nil".format(pid))
        }
        return builder.toString()
    }

    companion object {
        private const val BASE_TIME_MS = 1_720_000_000_000L
        private const val DEFAULT_POOLS = 2
        private const val DEFAULT_TOTAL_TESTS = 250
        /** Upper bound on devices we'll stamp per pool — matches the catalog. */
        private const val DEVICE_CATALOG_MAX = 20
        private const val STAGGER_WINDOW_MS = 5 * 60 * 1000L
        private const val INTER_TEST_GAP_MS = 10_000L


        private val ANDROID_DEVICES: List<DeviceInfo> = listOf(
            androidDevice("emulator-5554", "34", "Pixel 8 Pro", "Google"),
            androidDevice("emulator-5556", "34", "Pixel 7", "Google"),
            androidDevice("emulator-5558", "33", "Pixel 6a", "Google"),
            androidDevice("SM-S928U-1", "34", "Galaxy S24 Ultra", "Samsung"),
            androidDevice("SM-S928U-2", "34", "Galaxy S23", "Samsung"),
            androidDevice("SM-X710", "34", "Galaxy Tab S9", "Samsung", tablet = true),
            androidDevice("SM-A546", "33", "Galaxy A54", "Samsung"),
            androidDevice("OP11-01", "34", "OnePlus 11", "OnePlus"),
            androidDevice("OP11-02", "33", "OnePlus Nord 3", "OnePlus"),
            androidDevice("MI-13-01", "34", "Redmi Note 13 Pro", "Xiaomi"),
            androidDevice("MI-X6-01", "34", "Poco X6", "Xiaomi"),
            androidDevice("MOTO-G-2024", "33", "Moto G Power 2024", "Motorola"),
            androidDevice("MOTO-EDGE-40", "33", "Edge 40", "Motorola"),
            androidDevice("emulator-api-30", "30", "Emulator API 30", "Google"),
            androidDevice("emulator-api-29", "29", "Emulator API 29", "Google"),
            androidDevice("emulator-api-26", "26", "Emulator API 26", "Google", withVideo = false),
            androidDevice("NOTHING-2", "33", "Phone (2)", "Nothing"),
            androidDevice("XPERIA-5V", "33", "Xperia 5 V", "Sony"),
            androidDevice("FAIR-5", "33", "Fairphone 5", "Fairphone"),
            androidDevice("PIXEL-5", "33", "Pixel 5", "Google"),
        )

        private val IOS_DEVICES: List<DeviceInfo> = listOf(
            appleDevice("SIM-15PMAX", "17.4", "iPhone 15 Pro Max"),
            appleDevice("SIM-15P", "17.3", "iPhone 15 Pro"),
            appleDevice("SIM-15", "17.2", "iPhone 15"),
            appleDevice("SIM-14P", "17.1", "iPhone 14 Pro"),
            appleDevice("SIM-14", "16.6", "iPhone 14"),
            appleDevice("SIM-13m", "16.5", "iPhone 13 mini"),
            appleDevice("SIM-SE3", "17.0", "iPhone SE (3rd gen)"),
            appleDevice("SIM-IPAD-PRO-129", "17.4", "iPad Pro 12.9\"", tablet = true),
            appleDevice("SIM-IPAD-AIR-5", "16.7", "iPad Air (5th gen)", tablet = true),
            appleDevice("SIM-IPAD-MINI-6", "16.4", "iPad mini (6th gen)", tablet = true),
            appleDevice("SIM-15-SIM", "17.2", "Simulator iPhone 15"),
            appleDevice("SIM-14-SIM", "16.4", "Simulator iPhone 14"),
            appleDevice("SIM-IPAD-11-SIM", "17.0", "Simulator iPad Pro 11\"", tablet = true),
            appleDevice("SIM-13", "16.3", "iPhone 13"),
            appleDevice("SIM-12", "16.1", "iPhone 12"),
            appleDevice("SIM-11", "15.8", "iPhone 11"),
            appleDevice("SIM-IPAD-10", "17.3", "iPad (10th gen)", tablet = true),
            appleDevice("SIM-IPAD-9", "16.6", "iPad (9th gen)", tablet = true),
            appleDevice("SIM-SE-SIM", "17.1", "Simulator iPhone SE"),
            appleDevice("SIM-IPAD-PRO-129-SIM", "17.4", "Simulator iPad Pro 12.9\"", tablet = true),
        )

        private val TEST_CLASSES = listOf(
            "com.example.app.auth.LoginActivityTest",
            "com.example.app.auth.PasswordResetTest",
            "com.example.app.settings.SettingsFlowTest",
            "com.example.app.settings.NotificationsPrefsTest",
            "com.example.app.checkout.CheckoutFlowTest",
            "com.example.app.checkout.PaymentSheetTest",
            "com.example.app.cart.CartActivityTest",
            "com.example.app.catalog.SearchFragmentTest",
            "com.example.app.catalog.ProductDetailTest",
            "com.example.app.orders.OrderHistoryTest",
            "com.example.app.orders.OrderTrackingTest",
            "com.example.app.profile.ProfileActivityTest",
            "com.example.app.profile.AvatarUploaderTest",
            "com.example.app.chat.ConversationListTest",
            "com.example.app.chat.ThreadDetailTest",
            "com.example.app.notifications.PushTokenTest",
            "com.example.app.onboarding.OnboardingFlowTest",
            "com.example.app.deeplink.DeeplinkRouterTest",
            "com.example.app.upload.PhotoUploaderTest",
            "com.example.app.map.MapScreenTest",
        )

        private val METHODS = listOf(
            "displays_default_state",
            "submits_form_successfully",
            "shows_validation_error_for_missing_field",
            "shows_validation_error_for_invalid_format",
            "renders_empty_list_placeholder",
            "renders_skeleton_while_loading",
            "restores_state_after_config_change",
            "handles_offline_mode_gracefully",
            "retries_failed_requests_up_to_N_times",
            "navigates_to_detail_on_tap",
            "dismisses_modal_on_outside_tap",
            "preserves_scroll_position_on_rotation",
            "switches_theme_without_restart",
            "authenticates_via_biometric",
            "accepts_EULA_and_proceeds",
            "shows_analytics_opt_in_on_first_launch",
            "renders_localized_copy_correctly",
            "triggers_deep_link_from_notification",
            "reports_crash_via_bug_reporter",
            "submits_payment_with_3DS_challenge",
        )

        private fun androidDevice(
            serial: String,
            api: String,
            model: String,
            manufacturer: String,
            tablet: Boolean = false,
            withVideo: Boolean = true,
        ): DeviceInfo {
            // `tablet` is informational only; `DeviceInfo` doesn't carry a
            // tablet flag today. Fixture HTML derives `is_tablet=false` from
            // Kotlin currently — future model change would surface it here.
            val features = mutableListOf(DeviceFeature.SCREENSHOT)
            if (withVideo) features += DeviceFeature.VIDEO
            return DeviceInfo(
                operatingSystem = OperatingSystem(api),
                serialNumber = serial,
                model = model,
                manufacturer = manufacturer,
                networkState = NetworkState.CONNECTED,
                deviceFeatures = features,
                healthy = true,
            )
        }

        private fun appleDevice(serial: String, os: String, model: String, tablet: Boolean = false): DeviceInfo {
            // iPads in the fixture skip VIDEO capture, matching real marathon
            // apple vendor behavior for form-factor gating; iPhones get it.
            val features = mutableListOf(DeviceFeature.SCREENSHOT)
            if (!tablet) features += DeviceFeature.VIDEO
            return DeviceInfo(
                operatingSystem = OperatingSystem(os),
                serialNumber = serial,
                model = model,
                manufacturer = "Apple",
                networkState = NetworkState.CONNECTED,
                deviceFeatures = features,
                healthy = true,
            )
        }
    }
}
