package com.malinskiy.marathon.report.junit

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.report.Reporter

internal class FinalJUnitReporter(private val jUnitWriter: JUnitWriter) : Reporter {
    override fun generate(executionReport: ExecutionReport) {
        executionReport.testEvents.filter { it.final }.forEach { event ->
            jUnitWriter.testFinished(event.poolId, event.device, event.testResult)
        }
    }
}
