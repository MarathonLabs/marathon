package com.malinskiy.marathon.report.html

interface HtmlReportPrinter {
    fun print(testReport: Summary)
}