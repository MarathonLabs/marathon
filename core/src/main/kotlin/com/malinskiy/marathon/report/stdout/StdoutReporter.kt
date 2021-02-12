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
        summary.pools.forEach {
            cliReportBuilder.appendLine("Device pool ${it.poolId.name}:")
            cliReportBuilder.appendLine("\t${it.passed.size} passed, ${it.failed.size} failed, ${it.ignored.size} ignored tests")

            if(it.failed.isNotEmpty()){
                cliReportBuilder.appendLine("\tFailed tests:")
                it.failed.forEach { testName -> cliReportBuilder.appendLine("\t\t$testName") }
            }

            cliReportBuilder.appendLine("\tFlakiness overhead: ${it.rawDurationMillis - it.durationMillis}ms")
            cliReportBuilder.appendLine("\tRaw: ${it.rawPassed} passed, ${it.rawFailed} failed, ${it.rawIgnored} ignored, ${it.rawIncomplete} incomplete tests")
        }

        val hours = TimeUnit.MILLISECONDS.toHours(timer.elapsedTimeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timer.elapsedTimeMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timer.elapsedTimeMillis) % 60
        cliReportBuilder.appendLine("Total time: ${hours}H ${minutes}m ${seconds}s")

        println(cliReportBuilder)
    }
}
