package com.malinskiy.marathon.report.stdout

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.report.ProgressReporter
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.time.Timer
import org.apache.commons.lang3.time.DurationFormatUtils
import kotlin.math.roundToInt

class StdoutProgressReporter(private val timer: Timer) : ProgressReporter {
    override fun testFailed(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        post("failed", progress, poolId, deviceSerial, testName)
    }

    override fun testPassed(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        post("passed", progress, poolId, deviceSerial, testName)
    }

    override fun testIgnored(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        post("ignored", progress, poolId, deviceSerial, testName)
    }

    override fun testStarted(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        post("started", progress, poolId, deviceSerial, testName)
    }

    override fun testIncomplete(progress: Float, poolId: String, deviceSerial: String, testName: String) {
        post("incomplete", progress, poolId, deviceSerial, testName)
    }

    private fun post(status: String, progress: Float, poolId: String, deviceSerial: String, testName: String) {
        println("${toPercent(progress)} | [$poolId]-[$deviceSerial] $testName} $status")
    }

    private fun toPercent(float: Float): String {
        val percent = (float * HUNDRED_PERCENT_IN_FLOAT).roundToInt()
        val format = "%02d%%"
        return String.format(format, percent)
    }


    companion object {
        const val HUNDRED_PERCENT_IN_FLOAT: Float = 100.0f
    }

    override fun end(executionReport: ExecutionReport) {
        val summary = executionReport.summary
        if (summary.pools.isEmpty()) return

        val cliReportBuilder = StringBuilder().appendLine("Marathon run finished:")
        summary.pools.forEach { poolSummary ->
            cliReportBuilder.appendLine("Device pool ${poolSummary.poolId.name}:")
            cliReportBuilder.appendLine("\t${poolSummary.passed.size} passed, ${poolSummary.failed.size} failed, ${poolSummary.ignored.size} ignored tests")

            if(poolSummary.failed.isNotEmpty()){
                cliReportBuilder.appendLine("\tFailed tests:")
                poolSummary.failed
                    .map { it.toTestName() }
                    .toSortedSet()
                    .forEach { testName -> cliReportBuilder.appendLine("\t\t$testName") }
            }

            cliReportBuilder.appendLine("\tFlakiness overhead: ${formatDuration(poolSummary.rawDurationMillis - poolSummary.durationMillis)}")
            cliReportBuilder.appendLine("\tRaw: ${poolSummary.rawPassed.size} passed, ${poolSummary.rawFailed.size} failed, ${poolSummary.rawIgnored.size} ignored, ${poolSummary.rawIncomplete.size} incomplete tests")

            if(poolSummary.rawFailed.isNotEmpty()){
                cliReportBuilder.appendLine("\tFailed tests:")
                poolSummary.rawFailed
                    .groupBy { it.toTestName() }
                    .toSortedMap()
                    .mapValues { it.value.size }
                    .forEach { (testName, count) ->
                        cliReportBuilder.appendLine("\t\t$testName failed $count time(s)")
                    }
            }

            if(poolSummary.rawIncomplete.isNotEmpty()){
                cliReportBuilder.appendLine("\tIncomplete tests:")
                poolSummary.rawIncomplete
                    .groupBy { it.toTestName() }
                    .toSortedMap()
                    .mapValues { it.value.size }
                    .forEach { (testName, count) ->
                        cliReportBuilder.appendLine("\t\t$testName incomplete $count time(s)")
                    }
            }
        }
        cliReportBuilder.appendLine("Total time: ${formatDuration(timer.elapsedTimeMillis)}")

        println(cliReportBuilder)
    }

    private fun formatDuration(millis: Long) = if(millis > 0) DurationFormatUtils.formatDuration(millis, "H'H' mm'm' ss's'") else "0s"
}
