package com.malinskiy.marathon.report.stdout

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.report.Reporter
import com.malinskiy.marathon.time.Timer
import org.apache.commons.lang3.time.DurationFormatUtils

class StdoutReporter(private val timer: Timer) : Reporter {
    override fun generate(executionReport: ExecutionReport) {
        val summary = executionReport.summary
        if (summary.pools.isEmpty()) return

        val cliReportBuilder = StringBuilder().appendLine("Marathon run finished:")
        summary.pools.forEach { poolSummary ->
            cliReportBuilder.appendLine("Device pool ${poolSummary.poolId.name}:")
            cliReportBuilder.appendLine("\t${poolSummary.passed.size} passed, ${poolSummary.failed.size} failed, ${poolSummary.ignored.size} ignored tests")

            if(poolSummary.failed.isNotEmpty()){
                cliReportBuilder.appendLine("\tFailed tests:")
                poolSummary.failed
                    .toSortedSet()
                    .forEach { testName -> cliReportBuilder.appendLine("\t\t$testName") }
            }

            cliReportBuilder.appendLine("\tFlakiness overhead: ${formatDuration(poolSummary.rawDurationMillis - poolSummary.durationMillis)}$")
            cliReportBuilder.appendLine("\tRaw: ${poolSummary.rawPassed.size} passed, ${poolSummary.rawFailed.size} failed, ${poolSummary.rawIgnored.size} ignored, ${poolSummary.rawIncomplete.size} incomplete tests")

            if(poolSummary.rawFailed.isNotEmpty()){
                cliReportBuilder.appendLine("\tFailed tests:")
                poolSummary.rawFailed
                    .groupBy { it }
                    .toSortedMap()
                    .mapValues { it.value.size }
                    .forEach { (testName, count) ->
                        cliReportBuilder.appendLine("\t\t$testName failed $count time(s)")
                    }
            }

            if(poolSummary.rawIncomplete.isNotEmpty()){
                cliReportBuilder.appendLine("\tIncomplete tests:")
                    poolSummary.rawIncomplete
                        .groupBy { it }
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
