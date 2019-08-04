package com.malinskiy.marathon.report

import com.google.gson.Gson
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.report.internal.DeviceInfoReporter
import com.malinskiy.marathon.report.internal.TestResultRepo
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.File


class SummaryCompilerTest : Spek({

    val configuration = Configuration(name = "",
            outputDir = File("src/test/resources/output/"),
            analyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics,
            poolingStrategy = null,
            shardingStrategy = null,
            sortingStrategy = null,
            batchingStrategy = null,
            flakinessStrategy = null,
            retryStrategy = null,
            filteringConfiguration = null,
            ignoreFailures = null,
            isCodeCoverageEnabled = null,
            fallbackToScreenshots = null,
            strictMode = null,
            uncompletedTestRetryQuota = null,
            testClassRegexes = null,
            includeSerialRegexes = null,
            excludeSerialRegexes = null,
            testBatchTimeoutMillis = null,
            testOutputTimeoutMillis = null,
            debug = null,
            vendorConfiguration = object : VendorConfiguration {
                override fun testParser(): TestParser? = null
                override fun deviceProvider(): DeviceProvider? = null
                override fun logConfigurator(): MarathonLogConfigurator? = null
                override fun preferableRecorderType(): DeviceFeature? = null
            },
            analyticsTracking = false)

    val fileManager = FileManager(configuration.outputDir)
    val gson = Gson()
    val testResultReporter = TestResultRepo(fileManager, gson)
    val deviceInfoReporter = DeviceInfoReporter(fileManager, gson)
    println(configuration.outputDir.absolutePath)

    given("a summary compiler ") {
        val summaryCompiler = SummaryCompiler(deviceInfoSerializer = deviceInfoReporter,
                configuration = configuration,
                testResultSerializer = testResultReporter)
        on("a list of test Results ") {
            val tests = summaryCompiler.compile(listOf(DevicePoolId("myPool"))
            ).pools.flatMap { it.tests }

            it("should not include the INCOMPLETE test") {
                tests.filter { it.status == TestStatus.INCOMPLETE }.count() shouldBe 0
            }

            it("should include 1 PASSED test") {
                tests.filter { it.status == TestStatus.PASSED }.count() shouldBe 1
            }
            it("should include 1 FAILED test") {
                tests.filter { it.status == TestStatus.FAILURE }.count() shouldBe 1
            }
        }
    }
})
