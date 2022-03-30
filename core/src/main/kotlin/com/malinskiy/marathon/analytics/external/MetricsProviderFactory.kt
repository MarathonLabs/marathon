package com.malinskiy.marathon.analytics.external

import com.malinskiy.marathon.analytics.external.influx.InfluxDbProvider
import com.malinskiy.marathon.analytics.external.influx2.InfluxDb2Provider
import com.malinskiy.marathon.analytics.metrics.remote.graphite.GraphiteDataSource
import com.malinskiy.marathon.analytics.metrics.remote.graphite.QueryableGraphiteClient
import com.malinskiy.marathon.analytics.metrics.remote.influx.InfluxDBDataSource
import com.malinskiy.marathon.analytics.metrics.remote.influx2.InfluxDB2DataSource
import com.malinskiy.marathon.config.AnalyticsConfiguration.GraphiteConfiguration
import com.malinskiy.marathon.config.AnalyticsConfiguration.InfluxDb2Configuration
import com.malinskiy.marathon.config.AnalyticsConfiguration.InfluxDbConfiguration
import com.malinskiy.marathon.config.Configuration

internal class MetricsProviderFactory(configuration: Configuration) {
    private val configuration = configuration.analyticsConfiguration

    fun create(): MetricsProvider {
        return when (configuration) {
            is InfluxDbConfiguration -> createInfluxDBMetricsProvider(configuration, NoOpMetricsProvider())
            is InfluxDb2Configuration -> createInfluxDB2MetricsProvider(configuration, NoOpMetricsProvider())
            is GraphiteConfiguration -> createGraphiteMetricsProvider(configuration)
            else -> NoOpMetricsProvider()
        }
    }

    private fun createInfluxDBMetricsProvider(configuration: InfluxDbConfiguration, fallback: MetricsProvider): MetricsProvider {
        return try {
            val db = InfluxDbProvider(configuration).createDb()
            val dataSource = InfluxDBDataSource(db, configuration.dbName, configuration.retentionPolicyConfiguration.name)
            MetricsProviderImpl(dataSource)
        } catch (e: Exception) {
            fallback
        }
    }

    private fun createInfluxDB2MetricsProvider(configuration: InfluxDb2Configuration, fallback: MetricsProvider): MetricsProvider {
        return try {
            val db = InfluxDb2Provider(configuration).createDb()
            val dataSource = InfluxDB2DataSource(db, configuration.bucket)
            MetricsProviderImpl(dataSource)
        } catch (e: Exception) {
            fallback
        }
    }

    private fun createGraphiteMetricsProvider(configuration: GraphiteConfiguration): MetricsProvider {
        return MetricsProviderImpl(GraphiteDataSource(QueryableGraphiteClient(configuration.host), configuration.prefix))
    }
}
