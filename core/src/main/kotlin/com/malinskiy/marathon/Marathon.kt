package com.malinskiy.marathon

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.LocalTracker
import com.malinskiy.marathon.analytics.Tracker
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.Scheduler
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.report.SummaryCompiler
import com.malinskiy.marathon.report.html.HtmlSummaryPrinter
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
    private val summaryCompiler = SummaryCompiler(configuration, fileManager, gson)
    private val summaryPrinter = HtmlSummaryPrinter(gson, configuration.outputDir)

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
        val jUnitReporter = JUnitReporter(fileManager)
        val testResultSerializer = TestResultSerializer(fileManager, gson)
        return LocalTracker(fileManager, gson, jUnitReporter, testResultSerializer)
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

        summaryPrinter.print(summaryCompiler.compile(scheduler.getPools()))

        val hours = TimeUnit.MICROSECONDS.toHours(timeMillis)
        val minutes = TimeUnit.MICROSECONDS.toMinutes(timeMillis)
        val seconds = TimeUnit.MICROSECONDS.toSeconds(timeMillis)

        log.info { "Total time: ${hours}H ${minutes}m ${seconds}s" }

        deviceProvider.terminate()

        return false
    }
}
