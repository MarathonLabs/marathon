package com.malinskiy.marathon.report.html

import com.google.gson.Gson
import com.malinskiy.marathon.report.Summary
import com.malinskiy.marathon.report.SummaryPrinter
import java.io.File

class HtmlSummaryPrinter(private val gson: Gson,
                         private val rootOutput: File) : SummaryPrinter {
    override fun print(summary: Summary) {
        writeHtmlReport(gson, summary, rootOutput)
    }
}