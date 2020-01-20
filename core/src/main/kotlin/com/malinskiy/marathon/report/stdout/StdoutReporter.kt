package com.malinskiy.marathon.report.stdout

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.report.Reporter
import com.malinskiy.marathon.time.Timer
import java.util.concurrent.TimeUnit

class StdoutReporter(private val timer: Timer) : Reporter {
    override fun generate(executionReport: ExecutionReport) {
        val summary = executionReport.summary
        if (summary.pools.isEmpty()) return

        val cliReportBuilder = StringBuilder().appendln("Marathon run finished:")
        summary.pools.forEach {
            cliReportBuilder.appendln(
                "Device pool ${it.poolId.name}:\n" +
                        "\t${it.passed} passed, ${it.failed} failed, ${it.ignored} ignored tests\n" +
                        "\t${it.fromCache} from cache\n" +
                        "\tFlakiness overhead: ${it.rawDurationMillis - it.durationMillis}ms\n" +
                        "\tRaw: ${it.rawPassed} passed, ${it.rawFailed} failed, ${it.rawIgnored} ignored, ${it.rawIncomplete} incomplete tests"
            )
        }

        val hours = TimeUnit.MILLISECONDS.toHours(timer.elapsedTimeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timer.elapsedTimeMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timer.elapsedTimeMillis) % 60
        cliReportBuilder.appendln("Total time: ${hours}H ${minutes}m ${seconds}s")

        println(cliReportBuilder)
    }
}
