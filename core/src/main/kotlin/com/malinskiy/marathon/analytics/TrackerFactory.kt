package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.analytics.external.graphite.BasicGraphiteClient
import com.malinskiy.marathon.analytics.external.graphite.GraphiteTracker
import com.malinskiy.marathon.analytics.external.influx.InfluxDbProvider
import com.malinskiy.marathon.analytics.external.influx.InfluxDbTracker
import com.malinskiy.marathon.analytics.external.influx2.InfluxDb2Provider
import com.malinskiy.marathon.analytics.external.influx2.InfluxDb2Tracker
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.analytics.internal.sub.DelegatingTrackerInternal
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReportGenerator
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternal
import com.malinskiy.marathon.config.AnalyticsConfiguration.GraphiteConfiguration
import com.malinskiy.marathon.config.AnalyticsConfiguration.InfluxDb2Configuration
import com.malinskiy.marathon.config.AnalyticsConfiguration.InfluxDbConfiguration
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.report.ProgressReporter
import com.malinskiy.marathon.log.MarathonLogging

internal class TrackerFactory(
    private val configuration: Configuration,
    private val track: Track,
    private val progressReporter: ProgressReporter,
) {

    val log = MarathonLogging.logger("TrackerFactory")

    fun create(): TrackerInternal {
        val defaultTrackers = mutableListOf<TrackerInternal>(createExecutionReportGenerator())

        val analyticsConfiguration = configuration.analyticsConfiguration
        val analyticsTracker = when (analyticsConfiguration) {
            is InfluxDbConfiguration -> createInfluxDbTracker(analyticsConfiguration)
            is InfluxDb2Configuration -> createInfluxDb2Tracker(analyticsConfiguration)
            is GraphiteConfiguration -> createGraphiteTracker(analyticsConfiguration)
            else -> null
        }
        if (analyticsTracker != null) {
            defaultTrackers.add(analyticsTracker)
        }

        val delegatingTrackerInternal = DelegatingTrackerInternal(defaultTrackers)
        val mappingTracker = MappingTracker(delegatingTrackerInternal)
        track.add(mappingTracker)

        return delegatingTrackerInternal
    }

    private fun createInfluxDbTracker(config: InfluxDbConfiguration): InfluxDbTracker? {
        val db = try {
            InfluxDbProvider(config, configuration.debug).createDb()
        } catch (e: Exception) {
            log.warn(e) { "Failed to reach InfluxDB at ${config.url}" }
            null
        }
        return db?.let { InfluxDbTracker(it, config.dbName, config.retentionPolicyConfiguration.name, config.readOnly) }
    }

    private fun createInfluxDb2Tracker(config: InfluxDb2Configuration): InfluxDb2Tracker? {
        val db = try {
            InfluxDb2Provider(config, configuration.debug).createDb()
        } catch (e: Exception) {
            log.warn(e) { "Failed to reach InfluxDB at ${config.url}" }
            null
        }
        return db?.let { InfluxDb2Tracker(it, config.readOnly) }
    }

    private fun createGraphiteTracker(config: GraphiteConfiguration): GraphiteTracker {
        return GraphiteTracker(BasicGraphiteClient(config.host, config.port ?: 2003, config.prefix), config.readOnly)
    }

    private fun createExecutionReportGenerator(): ExecutionReportGenerator {
        return ExecutionReportGenerator(progressReporter)
    }
}
