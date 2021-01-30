package com.malinskiy.marathon.analytics.external

import com.malinskiy.marathon.analytics.external.influx.InfluxDbProvider
import com.malinskiy.marathon.analytics.metrics.remote.graphite.GraphiteDataSource
import com.malinskiy.marathon.analytics.metrics.remote.graphite.QueryableGraphiteClient
import com.malinskiy.marathon.analytics.metrics.remote.influx.InfluxDBDataSource
import com.malinskiy.marathon.execution.AnalyticsConfiguration.Graphite
import com.malinskiy.marathon.execution.AnalyticsConfiguration.Influx
import com.malinskiy.marathon.execution.Configuration

internal class MetricsProviderFactory(configuration: Configuration) {
    private val configuration = configuration.analyticsConfiguration

    fun create(): MetricsProvider {
        return when (configuration) {
            is Influx -> createInfluxDBMetricsProvider(configuration, NoOpMetricsProvider())
            is Graphite -> createGraphiteMetricsProvider(configuration)
            else -> NoOpMetricsProvider()
        }
    }

    private fun createInfluxDBMetricsProvider(configuration: Influx, fallback: MetricsProvider): MetricsProvider {
        return try {
            val db = InfluxDbProvider(configuration).createDb()
            val dataSource = InfluxDBDataSource(db, configuration.dbName, configuration.retentionPolicyConfiguration.name)
            MetricsProviderImpl(dataSource)
        } catch (e: Exception) {
            fallback
        }
    }

    private fun createGraphiteMetricsProvider(configuration: Graphite): MetricsProvider {
        return MetricsProviderImpl(GraphiteDataSource(QueryableGraphiteClient(configuration.host), configuration.prefix))
    }
}
