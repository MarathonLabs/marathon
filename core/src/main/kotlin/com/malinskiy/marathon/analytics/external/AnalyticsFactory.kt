package com.malinskiy.marathon.analytics.external

import com.malinskiy.marathon.execution.Configuration

class AnalyticsFactory(configuration: Configuration) {

    private val metricsFactory = MetricsProviderFactory(configuration)

    fun create(): Analytics = Analytics(metricsFactory.create())
}
