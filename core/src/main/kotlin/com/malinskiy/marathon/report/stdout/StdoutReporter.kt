package com.malinskiy.marathon.report.stdout

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.report.Reporter
import com.malinskiy.marathon.time.Timer
import java.util.concurrent.TimeUnit

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

            cliReportBuilder.appendLine("\tFlakiness overhead: ${poolSummary.rawDurationMillis - poolSummary.durationMillis}ms")
            cliReportBuilder.appendLine("\tRaw: ${poolSummary.rawPassed.size} passed, ${poolSummary.rawFailed.size} failed, ${poolSummary.rawIgnored.size} ignored, ${poolSummary.rawIncomplete.size} incomplete tests")

            if(poolSummary.rawFailed.isNotEmpty()){
                cliReportBuilder.appendLine("\tFailed tests:")
                poolSummary.rawFailed
                    .groupBy { it }
                    .toSortedMap()
                    .mapValues { it.value.size }
                    .forEach { (testName, count) ->
                        cliReportBuilder.appendLine("\t\t$testName failed $count times")
                    }
            }

            if(poolSummary.rawIncomplete.isNotEmpty()){
                cliReportBuilder.appendLine("\tIncomplete tests:")
                    poolSummary.rawIncomplete
                        .groupBy { it }
                        .toSortedMap()
                        .mapValues { it.value.size }
                        .forEach { (testName, count) ->
                            cliReportBuilder.appendLine("\t\t$testName incomplete $count times")
                        }
            }
        }

        val hours = TimeUnit.MILLISECONDS.toHours(timer.elapsedTimeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timer.elapsedTimeMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timer.elapsedTimeMillis) % 60
        cliReportBuilder.appendLine("Total time: ${hours}H ${minutes}m ${seconds}s")

        println(cliReportBuilder)
    }
}
