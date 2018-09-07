package com.malinskiy.marathon.analytics

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.metrics.MetricsProviderFactory
import com.malinskiy.marathon.analytics.tracker.TrackerFactory
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.report.internal.DeviceInfoReporter
import com.malinskiy.marathon.report.internal.TestResultReporter

class AnalyticsFactory(configuration: Configuration,
                       fileManager: FileManager,
                       deviceInfoReporter: DeviceInfoReporter,
                       testResultReporter: TestResultReporter,
                       gson: Gson) {

    private val metricsFactory = MetricsProviderFactory(configuration)
    private val trackerFactory = TrackerFactory(configuration, fileManager, deviceInfoReporter, testResultReporter,
            gson)

    fun create(): Analytics = Analytics(trackerFactory.create(), metricsFactory.create())
}
