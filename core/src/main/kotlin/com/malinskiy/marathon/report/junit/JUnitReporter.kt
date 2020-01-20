package com.malinskiy.marathon.report.junit

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.report.Reporter

internal class JUnitReporter(private val jUnitWriter: JUnitWriter) : Reporter {
    override fun generate(executionReport: ExecutionReport) {
        jUnitWriter.prepareXMLReport(executionReport)
    }
}
