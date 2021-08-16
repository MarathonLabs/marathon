package com.malinskiy.marathon.analytics

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.external.influx.InfluxDbProvider
import com.malinskiy.marathon.analytics.external.influx.InfluxDbTracker
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.analytics.internal.sub.DelegatingTrackerInternal
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReportGenerator
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternal
import com.malinskiy.marathon.execution.AnalyticsConfiguration.InfluxDbConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.allure.AllureReporter
import com.malinskiy.marathon.report.raw.RawJsonReporter
import com.malinskiy.marathon.report.stdout.StdoutReporter
import com.malinskiy.marathon.time.Timer
import java.io.File

internal class TrackerFactory(
    private val configuration: Configuration,
    private val fileManager: FileManager,
    private val gson: Gson,
    private val timer: Timer,
    private val track: Track
) {

    val log = MarathonLogging.logger("TrackerFactory")

    fun create(): TrackerInternal {
        val defaultTrackers = mutableListOf<TrackerInternal>(createExecutionReportGenerator())

        if (configuration.analyticsConfiguration is InfluxDbConfiguration) {
            val config = configuration.analyticsConfiguration
            val influxDbTracker = createInfluxDbTracker(config)
            influxDbTracker?.let { defaultTrackers.add(it) }
        }

        val delegatingTrackerInternal = DelegatingTrackerInternal(defaultTrackers)
        val mappingTracker = MappingTracker(delegatingTrackerInternal)
        track + mappingTracker

        return delegatingTrackerInternal
    }

    private fun createInfluxDbTracker(config: InfluxDbConfiguration): InfluxDbTracker? {
        val db = try {
            InfluxDbProvider(config).createDb()
        } catch (e: Exception) {
            log.warn(e) { "Failed to reach InfluxDB at ${config.url}" }
            null
        }
        return db?.let { InfluxDbTracker(it) }
    }

    private fun createExecutionReportGenerator(): ExecutionReportGenerator {
        return ExecutionReportGenerator(
            listOf(
                RawJsonReporter(fileManager, gson),
                AllureReporter(configuration, File(configuration.outputDir, "allure-results")),
                StdoutReporter(timer)
            )
        )
    }
}
