package com.malinskiy.marathon

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.DelegatingTracker
import com.malinskiy.marathon.analytics.local.DeviceTracker
import com.malinskiy.marathon.analytics.local.JUnitTracker
import com.malinskiy.marathon.analytics.Tracker
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.Scheduler
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.report.CompositeSummaryPrinter
import com.malinskiy.marathon.report.SummaryCompiler
import com.malinskiy.marathon.report.SummaryPrinter
import com.malinskiy.marathon.report.debug.timeline.TimelineSummaryPrinter
import com.malinskiy.marathon.report.debug.timeline.TimelineSummarySerializer
import com.malinskiy.marathon.report.html.HtmlSummaryPrinter
import com.malinskiy.marathon.report.internal.DeviceInfoReporter
import com.malinskiy.marathon.report.internal.TestResultSerializer
import com.malinskiy.marathon.report.junit.JUnitReporter
import kotlinx.coroutines.experimental.runBlocking
import mu.KotlinLogging
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

class Marathon(val configuration: Configuration) {

    private val fileManager = FileManager(configuration.outputDir)
    private val gson = Gson()

    private val testResultSerializer = TestResultSerializer(fileManager, gson)
    private val deviceInfoSerializer = DeviceInfoReporter(fileManager, gson)

    private val summaryCompiler = SummaryCompiler(deviceInfoSerializer, testResultSerializer, configuration)

    private fun loadSummaryPrinter(): SummaryPrinter {
        val outputDir = configuration.outputDir
        val htmlSummaryPrinter = HtmlSummaryPrinter(gson, outputDir)
        if (configuration.debug) {
            return CompositeSummaryPrinter(listOf(htmlSummaryPrinter, TimelineSummaryPrinter(TimelineSummarySerializer(testResultSerializer), gson, outputDir)))
        }
        return htmlSummaryPrinter
    }

    private fun loadDeviceProvider(): DeviceProvider {
        val deviceProvider = ServiceLoader.load(DeviceProvider::class.java).first()
        deviceProvider.initialize(configuration.vendorConfiguration)
        return deviceProvider
    }

    private fun loadTestParser(): TestParser {
        val loader = ServiceLoader.load(TestParser::class.java)
        return loader.first()
    }

    private fun loadTracker(): Tracker {
        return DelegatingTracker(listOf(
                JUnitTracker(JUnitReporter(fileManager)),
                DeviceTracker(deviceInfoSerializer)
        ))
    }

    fun run(): Boolean {
        val testParser = loadTestParser()
        val deviceProvider = loadDeviceProvider()
        val tracker = loadTracker()

        val tests = testParser.extract(configuration.testApplicationOutput)

        val scheduler = Scheduler(deviceProvider, tracker, configuration, tests)

        if (configuration.outputDir.exists()) {
            log.info { "Output ${configuration.outputDir} already exists" }
            configuration.outputDir.deleteRecursively()
        }
        configuration.outputDir.mkdirs()

        val timeMillis = measureTimeMillis {
            runBlocking {
                scheduler.execute()
            }
        }

        val summaryPrinter = loadSummaryPrinter()
        summaryPrinter.print(summaryCompiler.compile(scheduler.getPools()))

        val hours = TimeUnit.MICROSECONDS.toHours(timeMillis)
        val minutes = TimeUnit.MICROSECONDS.toMinutes(timeMillis)
        val seconds = TimeUnit.MICROSECONDS.toSeconds(timeMillis)

        log.info { "Total time: ${hours}H ${minutes}m ${seconds}s" }

        deviceProvider.terminate()

        return false
    }
}
