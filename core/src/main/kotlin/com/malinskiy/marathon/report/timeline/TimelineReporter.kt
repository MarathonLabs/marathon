package com.malinskiy.marathon.report.timeline

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.Reporter
import java.io.File
import java.io.InputStream


class TimelineReporter(private val provider: TimelineSummaryProvider,
                       private val gson: Gson,
                       private val rootOutput: File) : Reporter {

    override fun generate(executionReport: ExecutionReport) {
        val htmlDir = File(rootOutput, "/html")
        htmlDir.mkdirs()
        val timelineDir = File(htmlDir, "/timeline")
        timelineDir.mkdirs()
        val indexHtmlFile = File(timelineDir, "index.html")

        val chartCss = File(timelineDir, "chart.css")
        inputStreamFromResources("timeline/chart.css").copyTo(chartCss.outputStream())

        val chartJs = File(timelineDir, "chart.js")
        inputStreamFromResources("timeline/chart.js").copyTo(chartJs.outputStream())

        val json = gson.toJson(provider.generate(executionReport))
        val index = inputStreamFromResources("timeline/index.html")
        val indexText = index.reader().readText()
        indexHtmlFile.writeText(indexText.replace("\${dataset}", json))
    }

    val logger = MarathonLogging.logger(TimelineSummaryProvider::class.java.simpleName)

    private fun inputStreamFromResources(path: String): InputStream = TimelineExecutionResult::class.java.classLoader.getResourceAsStream(path)
}
