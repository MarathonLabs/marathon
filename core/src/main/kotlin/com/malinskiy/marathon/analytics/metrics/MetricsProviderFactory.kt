package com.malinskiy.marathon.analytics.metrics

import com.malinskiy.marathon.analytics.metrics.remote.influx.InfluxMetricsProvider
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.Configuration

internal class MetricsProviderFactory(configuration: Configuration): MetricsProviderProvider {
    private val configuration = configuration.analyticsConfiguration

    override fun create(): MetricsProvider {
        return if (configuration is AnalyticsConfiguration.InfluxDbConfiguration) {
            InfluxMetricsProvider.createWithFallback(configuration, NoOpMetricsProvider())
        } else {
            NoOpMetricsProvider()
        }
    }
}
