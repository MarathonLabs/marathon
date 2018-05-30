package com.malinskiy.marathon.report.html

import com.google.gson.Gson
import com.malinskiy.marathon.report.HtmlSuite
import org.apache.commons.text.StringEscapeUtils
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Following file tree structure will be created:
 * - index.json
 * - suites/suiteId.json
 * - suites/deviceId/testId.json
 */

fun writeHtmlReport(gson: Gson, summary: Summary, rootOutput: File) {
    rootOutput.mkdirs()
    val outputDir = File(rootOutput, "/html")
    outputDir.mkdirs()

    val htmlIndexJson = gson.toJson(summary.toHtmlIndex())

    val formattedDate = SimpleDateFormat("HH:mm:ss z, MMM d yyyy").apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())

    val appJs = File(outputDir, "app.min.js")
    inputStreamFromResources("html-report/app.min.js").copyTo(appJs.outputStream())

    val appCss = File(outputDir, "app.min.css")
    inputStreamFromResources("html-report/app.min.css").copyTo(appCss.outputStream())

    // index.html is a page that can render all kinds of inner pages: Index, Suite, Test.
    val indexHtml = inputStreamFromResources("html-report/index.html").reader().readText()

    val indexHtmlFile = File(outputDir, "index.html")

    fun File.relativePathToHtmlDir(): String = outputDir.relativePathTo(this.parentFile).let { relativePath ->
        when (relativePath) {
            "" -> relativePath
            else -> "$relativePath/"
        }
    }

    indexHtmlFile.writeText(indexHtml
            .replace("\${relative_path}", indexHtmlFile.relativePathToHtmlDir())
            .replace("\${data_json}", "window.mainData = $htmlIndexJson")
            .replace("\${date}", formattedDate)
            .replace("\${log}", "")
    )

    val poolDir = File(outputDir, "suites").apply { mkdirs() }

    summary.pools.mapIndexed { suiteId, pool ->

        val poolJson = gson.toJson(pool.toHtmlSuite())
        val poolHtmlFile = File(poolDir, "$suiteId.html")

        poolHtmlFile.writeText(indexHtml
                .replace("\${relative_path}", poolHtmlFile.relativePathToHtmlDir())
                .replace("\${data_json}", "window.suite = $poolJson")
                .replace("\${date}", formattedDate)
                .replace("\${log}", "")
        )

        pool
                .tests
                .map { it to File(File(poolDir, "$suiteId"), it.device.serialNumber).apply { mkdirs() } }
                .map { (test, testDir) -> Triple(test, test.toHtmlFullTest(poolId = pool.poolId), testDir) }
                .forEach { (test, htmlTest, testDir) ->
                    val testJson = gson.toJson(htmlTest)
                    val testHtmlFile = File(testDir, "${htmlTest.id}.html")

                    testHtmlFile.writeText(indexHtml
                            .replace("\${relative_path}", testHtmlFile.relativePathToHtmlDir())
                            .replace("\${data_json}", "window.test = $testJson")
                            .replace("\${date}", formattedDate)
//                            .replace("\${log}", generateLogcatHtml(test.logcat))
                            .replace("\${log}", "")
                    )
                }
    }
}

/*
 * Fixed version of `toRelativeString()` from Kotlin stdlib that forces use of absolute file paths.
 * See https://youtrack.jetbrains.com/issue/KT-14056
*/

fun File.relativePathTo(base: File): String = absoluteFile.toRelativeString(base.absoluteFile)

fun inputStreamFromResources(path: String): InputStream = HtmlSuite::class.java.classLoader.getResourceAsStream(path)

fun generateLogcatHtml(logcatOutput: File): String = when (logcatOutput.exists()) {
    false -> ""
    true -> logcatOutput
            .readLines()
            .map { line -> """<div class="log__${cssClassForLogcatLine(line)}">${StringEscapeUtils.escapeXml11(line)}</div>""" }
            .fold(StringBuilder("""<div class="content"><div class="card log">""")) { stringBuilder, line ->
                stringBuilder.appendln(line)
            }
            .appendln("""</div></div>""")
            .toString()
}

fun cssClassForLogcatLine(logcatLine: String): String {
    // Logcat line example: `06-07 16:55:14.490  2100  2100 I MicroDetectionWorker: #onError(false)`
    // First letter is Logcat level.
    return when (logcatLine.firstOrNull { it.isLetter() }) {
        'V' -> "verbose"
        'D' -> "debug"
        'I' -> "info"
        'W' -> "warning"
        'E' -> "error"
        'A' -> "assert"
        else -> "default"
    }
}
