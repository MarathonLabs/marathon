package com.malinskiy.marathon

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.AnalyticsFactory
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.Scheduler
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.CompositeSummaryPrinter
import com.malinskiy.marathon.report.SummaryCompiler
import com.malinskiy.marathon.report.SummaryPrinter
import com.malinskiy.marathon.report.debug.timeline.TimelineSummaryPrinter
import com.malinskiy.marathon.report.debug.timeline.TimelineSummarySerializer
import com.malinskiy.marathon.report.html.HtmlSummaryPrinter
import com.malinskiy.marathon.report.internal.DeviceInfoReporter
import com.malinskiy.marathon.report.internal.TestResultReporter
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.experimental.runBlocking
import java.util.ServiceLoader
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

private val log = MarathonLogging.logger {}

class Marathon(val configuration: Configuration) {

    private val fileManager = FileManager(configuration.outputDir)
    private val gson = Gson()

    private val testResultReporter = TestResultReporter(fileManager, gson)
    private val deviceInfoReporter = DeviceInfoReporter(fileManager, gson)
    private val analyticsFactory = AnalyticsFactory(configuration, fileManager, deviceInfoReporter, testResultReporter,
            gson)

    private val summaryCompiler = SummaryCompiler(deviceInfoReporter, testResultReporter, configuration)

    private fun loadSummaryPrinter(): SummaryPrinter {
        val outputDir = configuration.outputDir
        val htmlSummaryPrinter = HtmlSummaryPrinter(gson, outputDir)
        if (configuration.debug) {
            return CompositeSummaryPrinter(listOf(
                    htmlSummaryPrinter,
                    TimelineSummaryPrinter(TimelineSummarySerializer(analyticsFactory.rawTestResultTracker), gson, outputDir)
            ))
        }
        return htmlSummaryPrinter
    }

    private fun loadDeviceProvider(vendorConfiguration: VendorConfiguration): DeviceProvider {
        var vendorDeviceProvider = vendorConfiguration.deviceProvider()
                ?: ServiceLoader.load(DeviceProvider::class.java).first()

        vendorDeviceProvider.initialize(configuration.vendorConfiguration)
        return vendorDeviceProvider
    }

    private fun loadTestParser(vendorConfiguration: VendorConfiguration): TestParser {
        val vendorTestParser = vendorConfiguration.testParser()
        if (vendorTestParser != null) {
            return vendorTestParser
        }
        val loader = ServiceLoader.load(TestParser::class.java)
        return loader.first()
    }

    fun run(): Boolean = runBlocking {
        MarathonLogging.debug = configuration.debug

        val testParser = loadTestParser(configuration.vendorConfiguration)
        val deviceProvider = loadDeviceProvider(configuration.vendorConfiguration)
        val analytics = analyticsFactory.create()

        val parsedTests = testParser.extract(configuration)
        val tests = applyTestFilters(parsedTests)

        log.info("Scheduling ${tests.size} tests")
        log.debug(tests.map { it.toTestName() }.joinToString(", "))
        val progressReporter = ProgressReporter()
        val scheduler = Scheduler(deviceProvider, analytics, configuration, tests, progressReporter)

        if (configuration.outputDir.exists()) {
            log.info { "Output ${configuration.outputDir} already exists" }
            configuration.outputDir.deleteRecursively()
        }
        configuration.outputDir.mkdirs()

        val timeMillis = measureTimeMillis {
            scheduler.execute()
        }

        val pools = scheduler.getPools()
        if (!pools.isEmpty()) {
            val summaryPrinter = loadSummaryPrinter()
            val summary = summaryCompiler.compile(scheduler.getPools())
            summaryPrinter.print(summary)
        }

        val hours = TimeUnit.MILLISECONDS.toHours(timeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis) % 60

        log.info { "Total time: ${hours}H ${minutes}m ${seconds}s" }
        analytics.terminate()
        analytics.close()
        deviceProvider.terminate()
        progressReporter.aggregateResult()
    }

    private fun applyTestFilters(parsedTests: List<Test>): List<Test> {
        var tests = parsedTests.filter { test ->
            configuration.testClassRegexes.all { it.matches(test.clazz) }
        }

        tests = configuration.filteringConfiguration.whitelist.map { it.filter(tests) }.flatten().toSet().toList()
        configuration.filteringConfiguration.blacklist.forEach { tests = it.filterNot(tests) }
        return tests
    }
}
