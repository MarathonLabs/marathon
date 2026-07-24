package com.malinskiy.marathon.report.html

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.analytics.internal.sub.PoolSummary
import com.malinskiy.marathon.analytics.internal.sub.Summary
import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.AttachmentType
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.extension.escape
import com.malinskiy.marathon.extension.relativePathTo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.io.FolderType
import com.malinskiy.marathon.report.HtmlAttempt
import com.malinskiy.marathon.report.HtmlDevice
import com.malinskiy.marathon.report.HtmlFullTest
import com.malinskiy.marathon.report.HtmlIndex
import com.malinskiy.marathon.report.HtmlLogAttempt
import com.malinskiy.marathon.report.HtmlPoolSummary
import com.malinskiy.marathon.report.HtmlShortTest
import com.malinskiy.marathon.report.HtmlTestLogDetails
import com.malinskiy.marathon.report.Reporter
import com.malinskiy.marathon.report.Status
import com.malinskiy.marathon.report.timeline.TimelineSummaryProvider
import com.malinskiy.marathon.test.toClassName
import java.io.File
import java.io.InputStream
import kotlin.math.roundToLong

class HtmlSummaryReporter(
    private val gson: Gson,
    private val fileManager: FileManager,
    private val rootOutput: File,
    private val configuration: Configuration,
    /**
     * Timeline data is inlined into the index HTML rather than living in a
     * separate `timeline/index.html` file — the old iframe layout was a
     * carryover from marathon's predecessor (Juno / Composer) where the
     * timeline chart shipped as its own bundle. React app now renders the
     * chart directly, so we hand it the payload alongside `HtmlIndex`.
     */
    private val timelineSummaryProvider: TimelineSummaryProvider = TimelineSummaryProvider(),
) : Reporter {

    /**
     * Emitted layout:
     * ```
     * html/index.html
     * html/pools/<pool>.html
     * html/pools/<pool>/<device>/<test>.html
     * html/pools/<pool>/<device>/logs/<test>.html
     * ```
     */
    override fun generate(executionReport: ExecutionReport) {
        val summary = executionReport.summary
        if (summary.pools.isEmpty()) return

        val htmlIndex = summary.toHtmlIndex()
        val htmlIndexJson = gson.toJson(htmlIndex)

        val outputDir = fileManager.createFolder(FolderType.HTML)

        inputStreamFromResources("html-report/app.min.js")
            .copyTo(File(outputDir, "app.min.js").outputStream())
        inputStreamFromResources("html-report/app.min.css")
            .copyTo(File(outputDir, "app.min.css").outputStream())

        val indexHtml = inputStreamFromResources("html-report/index.html").reader().readText()

        fun File.relativePathToHtmlDir(): String =
            outputDir.relativePathTo(parentFile).let { rel -> if (rel.isEmpty()) rel else "$rel/" }

        // Every page carries the run's generated-at timestamp so the React
        // shell can render the footer inside the main flow rather than
        // depending on the template's old `${date}` hook (which dangled
        // outside `#root`).
        fun render(target: File, payloadGlobal: String, payloadJson: String, extraJs: String = "") {
            val body = buildString {
                append("window.reportGeneratedAt = ").append(htmlIndex.generatedAtMs).append(";\n")
                append("window.").append(payloadGlobal).append(" = ").append(payloadJson).append(";")
                // Newline separator between globals keeps the reporter tests'
                // single-line extractor (`substringBefore('\n')`) happy while
                // still fitting the string-replace template contract.
                if (extraJs.isNotEmpty()) append('\n').append(extraJs)
            }
            target.writeText(
                indexHtml
                    .replace("\${relative_path}", target.relativePathToHtmlDir())
                    .replace("\${data_json}", body)
            )
        }

        // HomePage renders the timeline directly from `window.timeline`; no
        // separate iframe target on disk anymore. Only the root index page
        // ships the timeline blob.
        val timelineJson = gson.toJson(timelineSummaryProvider.generate(executionReport))
        render(
            File(outputDir, "index.html"),
            payloadGlobal = "mainData",
            payloadJson = htmlIndexJson,
            extraJs = "window.timeline = $timelineJson;",
        )

        val poolsDir = File(outputDir, "pools").apply { mkdirs() }

        summary.pools.forEach { pool ->
            val poolHtmlFile = File(poolsDir, "${pool.poolId.name}.html")
            render(poolHtmlFile, "pool", gson.toJson(pool.toHtmlPoolSummary()))

            pool.tests.forEach { finalResult ->
                val testDir = File(File(poolsDir, pool.poolId.name), finalResult.device.safeSerialNumber).apply { mkdirs() }
                val attemptResults = pool.attemptResults(finalResult)
                val attempts = attemptResults.mapIndexed { index, result ->
                    result.toHtmlAttempt(
                        poolId = pool.poolId.name,
                        attemptIndex = index,
                        final = result === finalResult,
                    )
                }
                val htmlTest = finalResult.toHtmlFullTest(pool.poolId.name, attempts)

                val testHtmlFile = File(testDir, "${htmlTest.id.escape().safePathLength()}.html")
                render(testHtmlFile, "test", gson.toJson(htmlTest))

                val logDir = File(testDir, "logs").apply { mkdirs() }
                val logDetails = toHtmlTestLogDetails(pool.poolId.name, htmlTest, attemptResults)
                val logHtmlFile = File(logDir, "${htmlTest.id.escape().safePathLength()}.html")
                render(logHtmlFile, "logs", gson.toJson(logDetails))
            }
        }
    }

    private fun DeviceInfo.toHtmlDevice(): HtmlDevice {
        val majorFromVersion = operatingSystem.version.substringBefore('.').toIntOrNull()
        return HtmlDevice(
            serial = safeSerialNumber,
            modelName = model,
            manufacturer = manufacturer,
            osVersion = operatingSystem.version,
            osMajor = majorFromVersion,
            networkState = networkState.name,
            features = deviceFeatures.map { it.name }.sorted(),
            isTablet = false,
            apiLevel = operatingSystem.version,
        )
    }

    private fun TestResult.artifactScreenshotPath(poolId: String): String {
        val relative = fileManager.createFile(FileType.SCREENSHOT, DevicePoolId(poolId), device, test, testBatchId)
            .relativePathTo(rootOutput)
        val full = File(rootOutput, relative)
        return if (device.deviceFeatures.contains(DeviceFeature.SCREENSHOT) && full.exists()) {
            "../../../../${relative.replace("#", "%23")}"
        } else ""
    }

    private fun TestResult.artifactVideoPaths(): List<String> {
        if (!device.deviceFeatures.contains(DeviceFeature.VIDEO)) return emptyList()
        return attachments
            .filter { it.type == AttachmentType.VIDEO && it.file.exists() }
            .map { it.file.relativePathTo(rootOutput).replace("#", "%23") }
            .map { "../../../../$it" }
    }

    private fun TestResult.artifactLogPath(poolId: String): String {
        val relative = fileManager.createFile(FileType.LOG, DevicePoolId(poolId), device, test, testBatchId)
            .relativePathTo(rootOutput)
        val full = File(rootOutput, relative)
        return if (full.exists()) "../../../../${relative.replace("#", "%23")}" else ""
    }

    /**
     * Chronological attempt sequence for one logical test — prior attempts
     * from `PoolSummary.retries` plus the final result, sorted by start time.
     */
    private fun PoolSummary.attemptResults(finalResult: TestResult): List<TestResult> {
        val priors: List<TestResult> = retries[finalResult]?.map(TestEvent::testResult) ?: emptyList()
        return (priors + finalResult).sortedBy { it.startTime }
    }

    private fun TestResult.toHtmlAttempt(poolId: String, attemptIndex: Int, final: Boolean) = HtmlAttempt(
        attemptIndex = attemptIndex,
        final = final,
        status = status.toHtmlStatus(),
        startTimeMs = startTime,
        endTimeMs = endTime,
        durationMillis = durationMillis(),
        batchId = testBatchId,
        device = device.toHtmlDevice(),
        stacktrace = stacktrace,
        screenshot = artifactScreenshotPath(poolId),
        videos = artifactVideoPaths(),
        logFile = artifactLogPath(poolId),
    )

    private fun TestResult.toHtmlFullTest(poolId: String, attempts: List<HtmlAttempt>): HtmlFullTest {
        val finalAttempt = attempts.first { it.final }
        val distinctDevices = attempts.map { it.device }.distinctBy { it.serial }
        return HtmlFullTest(
            poolId = poolId,
            packageName = test.pkg,
            className = test.clazz,
            name = test.method,
            id = "${test.toClassName()}.${test.method}",
            filename = "${test.toClassName()}.${test.method}".escape().safePathLength() + ".html",
            durationMillis = durationMillis(),
            status = finalAttempt.status,
            stacktrace = finalAttempt.stacktrace,
            startTimeMs = startTime,
            endTimeMs = endTime,
            batchId = testBatchId,
            device = finalAttempt.device,
            deviceId = device.safeSerialNumber,
            diagnosticVideo = device.deviceFeatures.contains(DeviceFeature.VIDEO),
            diagnosticScreenshots = device.deviceFeatures.contains(DeviceFeature.SCREENSHOT),
            screenshot = finalAttempt.screenshot,
            videos = finalAttempt.videos,
            logFile = finalAttempt.logFile,
            attempts = attempts,
            attemptCount = attempts.size,
            isFlaky = attempts.isFlakySequence(),
            distinctDevices = distinctDevices,
        )
    }

    /**
     * A test is flaky when its attempts include at least one PASSED and at least one FAILED outcome.
     * IGNORED/ASSUMPTION_FAILURE attempts do not count as either side of the flip.
     */
    private fun List<HtmlAttempt>.isFlakySequence(): Boolean {
        if (size < 2) return false
        val anyPassed = any { it.status == Status.Passed }
        val anyFailed = any { it.status == Status.Failed }
        return anyPassed && anyFailed
    }

    private fun TestStatus.toHtmlStatus() = when (this) {
        TestStatus.PASSED -> Status.Passed
        TestStatus.FAILURE -> Status.Failed
        TestStatus.IGNORED, TestStatus.ASSUMPTION_FAILURE -> Status.Ignored
        else -> Status.Failed
    }

    private fun PoolSummary.toHtmlPoolSummary(): HtmlPoolSummary {
        val perTestAttempts = tests.associateWith { finalResult ->
            attemptResults(finalResult).mapIndexed { index, result ->
                result.toHtmlAttempt(poolId = poolId.name, attemptIndex = index, final = result === finalResult)
            }
        }
        val flakyCount = perTestAttempts.values.count { it.isFlakySequence() }
        val allAttempts = perTestAttempts.values.flatten()
        val start = allAttempts.filter { it.startTimeMs > 0 }.minOfOrNull { it.startTimeMs } ?: 0L
        val end = allAttempts.maxOfOrNull { it.endTimeMs } ?: 0L
        return HtmlPoolSummary(
            id = poolId.name,
            tests = tests.map { it.toHtmlShortTest(perTestAttempts.getValue(it)) },
            passedCount = passed.size,
            failedCount = failed.size,
            ignoredCount = ignored.size,
            flakyCount = flakyCount,
            durationMillis = durationMillis,
            startTimeMs = start,
            endTimeMs = end,
            devices = devices.map { it.toHtmlDevice() },
        )
    }

    private fun TestResult.toHtmlShortTest(attempts: List<HtmlAttempt>): HtmlShortTest {
        val finalAttempt = attempts.first { it.final }
        return HtmlShortTest(
            id = "${test.toClassName()}.${test.method}",
            fileName = "${test.toClassName()}.${test.method}".escape().safePathLength() + ".html",
            packageName = test.pkg,
            className = test.clazz,
            name = test.method,
            durationMillis = durationMillis(),
            status = finalAttempt.status,
            startTimeMs = startTime,
            endTimeMs = endTime,
            batchId = testBatchId,
            deviceId = device.safeSerialNumber,
            device = finalAttempt.device,
            attemptCount = attempts.size,
            isFlaky = attempts.isFlakySequence(),
            devices = attempts.map { it.device.serial }.distinct(),
            osVersions = attempts.map { it.device.osVersion }.distinct(),
        )
    }

    private fun Summary.toHtmlIndex(): HtmlIndex {
        val perPool = pools.associateWith { it.toHtmlPoolSummary() }
        return HtmlIndex(
            generatedAtMs = System.currentTimeMillis(),
            title = configuration.name,
            totalFailed = pools.sumOf { it.failed.size },
            totalIgnored = pools.sumOf { it.ignored.size },
            totalPassed = pools.sumOf { it.passed.size },
            totalFlaky = perPool.values.sumOf { it.flakyCount },
            totalDuration = totalDuration(pools),
            averageDuration = averageDuration(pools),
            maxDuration = maxDuration(pools),
            minDuration = minDuration(pools),
            pools = perPool.values.toList(),
        )
    }

    internal fun totalDuration(poolSummaries: List<PoolSummary>): Long =
        poolSummaries.flatMap { it.tests }.sumOf { it.durationMillis() }

    internal fun averageDuration(poolSummaries: List<PoolSummary>): Long {
        val perPool = durationPerPool(poolSummaries)
        return if (perPool.isEmpty()) 0L else perPool.average().roundToLong()
    }

    internal fun minDuration(poolSummaries: List<PoolSummary>): Long =
        durationPerPool(poolSummaries).minOrNull() ?: 0L

    internal fun maxDuration(poolSummaries: List<PoolSummary>): Long =
        durationPerPool(poolSummaries).maxOrNull() ?: 0L

    private fun durationPerPool(poolSummaries: List<PoolSummary>): List<Long> =
        poolSummaries.map { pool -> pool.tests.sumOf { it.durationMillis() } }

    private fun toHtmlTestLogDetails(
        poolId: String,
        fullTest: HtmlFullTest,
        attemptResults: List<TestResult>,
    ): HtmlTestLogDetails =
        HtmlTestLogDetails(
            poolId = poolId,
            testId = fullTest.id,
            displayName = fullTest.name,
            attempts = fullTest.attempts.mapIndexed { index, attempt ->
                val result = attemptResults[index]
                HtmlLogAttempt(
                    attemptIndex = attempt.attemptIndex,
                    final = attempt.final,
                    status = attempt.status,
                    deviceId = attempt.device.serial,
                    device = attempt.device,
                    // Log paths on attempts are pool-root relative (`../../../..`),
                    // logs page sits one dir deeper, so prepend one more `..`.
                    logPath = if (attempt.logFile.isEmpty()) "" else "../${attempt.logFile}",
                    logBody = readInlineLogBody(poolId, result),
                )
            },
        )

    /**
     * Slurp the on-disk log file into the payload so the viewer can render
     * without a runtime `fetch()` — Chromium blocks fetch() under `file://`,
     * and users typically double-click `index.html`.
     *
     * Applies a size cap so a runaway log doesn't inflate the HTML to the
     * point of browser sadness. When truncated we append a marker rather
     * than silently drop the tail. If the file is too big to inline at all
     * we return null and let the UI fall back to `logPath`.
     */
    private fun readInlineLogBody(poolId: String, result: TestResult): String? {
        val logFile = fileManager.createFile(
            FileType.LOG,
            DevicePoolId(poolId),
            result.device,
            result.test,
            result.testBatchId,
        )
        if (!logFile.exists()) return null
        val length = logFile.length()
        return when {
            length == 0L -> ""
            length <= LOG_INLINE_CAP_BYTES -> logFile.readText(Charsets.UTF_8)
            else -> logFile.inputStream().use { stream ->
                val buf = ByteArray(LOG_INLINE_CAP_BYTES)
                var read = 0
                while (read < buf.size) {
                    val n = stream.read(buf, read, buf.size - read)
                    if (n <= 0) break
                    read += n
                }
                val head = String(buf, 0, read, Charsets.UTF_8)
                "$head\n… [truncated by report — ${length - LOG_INLINE_CAP_BYTES} bytes not shown; open ${logFile.name} on disk for the full log]"
            }
        }
    }

    companion object {
        /**
         * Per-attempt log inline cap. 1 MiB keeps the emitted HTML shell
         * bounded on typical test runs while covering the common android /
         * ios logcat volumes we see in practice.
         */
        const val LOG_INLINE_CAP_BYTES = 1_048_576
    }
}

private fun String.safePathLength(): String =
    if (length >= 128) substring(0 until 128) else this

private fun inputStreamFromResources(path: String): InputStream =
    HtmlSummaryReporter::class.java.classLoader.getResourceAsStream(path)!!
