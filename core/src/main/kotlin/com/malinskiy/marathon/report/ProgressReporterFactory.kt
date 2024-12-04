package com.malinskiy.marathon.report

import com.google.gson.Gson
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.ProgressConfiguration
import com.malinskiy.marathon.config.ProgressReporterConfiguration
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.report.allure.AllureReporter
import com.malinskiy.marathon.report.api.ApiV1ProgressReporter
import com.malinskiy.marathon.report.bill.BillingReporter
import com.malinskiy.marathon.report.device.DeviceInfoJsonReporter
import com.malinskiy.marathon.report.html.HtmlSummaryReporter
import com.malinskiy.marathon.report.junit.JUnitReporter
import com.malinskiy.marathon.report.raw.RawJsonReporter
import com.malinskiy.marathon.report.stdout.StdoutProgressReporter
import com.malinskiy.marathon.report.teamcity.TeamCityProgressReporter
import com.malinskiy.marathon.report.test.TestJsonReporter
import com.malinskiy.marathon.report.timeline.TimelineReporter
import com.malinskiy.marathon.report.timeline.TimelineSummaryProvider
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.usageanalytics.tracker.UsageTracker
import java.io.File

class ProgressReporterFactory(
    private val configuration: Configuration,
    private val fileManager: FileManager,
    private val gson: Gson,
    private val usageTracker: UsageTracker,
    private val timer: Timer
) {
    fun create(): CompositeProgressReporter {
        when (configuration.progressConfiguration) {
            ProgressConfiguration.Auto -> {
                //Map to a default custom configuration
                val reporters = mutableListOf(
                    ProgressReporterConfiguration.Device,
                    ProgressReporterConfiguration.Billing,
                    ProgressReporterConfiguration.JUnit,
                    ProgressReporterConfiguration.Timeline,
                    ProgressReporterConfiguration.Raw,
                    ProgressReporterConfiguration.Test,
                    ProgressReporterConfiguration.Allure,
                    ProgressReporterConfiguration.Html,
                    ProgressReporterConfiguration.Stdout,
                )
                // Teamcity predefined variables: https://www.jetbrains.com/help/teamcity/predefined-build-parameters.html#84e0b866
                if (System.getenv().containsValue("TEAMCITY_VERSION")) {
                    reporters.add(ProgressReporterConfiguration.Teamcity)
                }

                return create(ProgressConfiguration.Custom(reporters))
            }

            is ProgressConfiguration.Custom -> {
                return create(configuration.progressConfiguration as ProgressConfiguration.Custom)
            }
        }

    }

    fun create(cfg: ProgressConfiguration.Custom): CompositeProgressReporter {
        val reporters: List<ProgressReporter> = cfg.values.map {
            val reporter = when (it) {
                is ProgressReporterConfiguration.ApiV1 -> ApiV1ProgressReporter(it.target, it.run_id)
                ProgressReporterConfiguration.Stdout -> StdoutProgressReporter(timer)
                ProgressReporterConfiguration.Teamcity -> TeamCityProgressReporter()
                ProgressReporterConfiguration.Allure -> AllureReporter(
                    configuration,
                    File(configuration.outputDir, "allure-results")
                )

                ProgressReporterConfiguration.Billing -> BillingReporter(fileManager, gson, usageTracker)
                ProgressReporterConfiguration.Device -> DeviceInfoJsonReporter(fileManager, gson)
                ProgressReporterConfiguration.Html -> HtmlSummaryReporter(gson, fileManager, configuration.outputDir, configuration)
                ProgressReporterConfiguration.JUnit -> JUnitReporter(configuration.outputDir)
                ProgressReporterConfiguration.Raw -> RawJsonReporter(fileManager, gson)
                ProgressReporterConfiguration.Test -> TestJsonReporter(fileManager, gson)
                ProgressReporterConfiguration.Timeline -> TimelineReporter(TimelineSummaryProvider(), gson, configuration.outputDir)
            }
            reporter
        }.toList()
        return CompositeProgressReporter(reporters)
    }
}
