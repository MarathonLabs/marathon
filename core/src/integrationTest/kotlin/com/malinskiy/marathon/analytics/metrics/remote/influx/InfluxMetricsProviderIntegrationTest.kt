package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.external.influx.InfluxDbProvider
import com.malinskiy.marathon.analytics.external.influx.InfluxDbTracker
import com.malinskiy.marathon.analytics.metrics.remote.BaseMetricsProviderIntegrationTest
import com.malinskiy.marathon.analytics.metrics.remote.getTestEvents
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import org.influxdb.InfluxDB
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

class InfluxMetricsProviderIntegrationTest : BaseMetricsProviderIntegrationTest() {

    override fun createRemoteDataSource() = InfluxDBDataSource(influxDb, dbName, rpName)

    private companion object {
        const val dbName = "marathonDb"
        const val rpName = "rpMarathon"

        val container: KInfluxDBContainer = KInfluxDBContainer().withAuthEnabled(false)

        val influxDb: InfluxDB by lazy {
            InfluxDbProvider(
                AnalyticsConfiguration.Influx(
                    url = container.url,
                    dbName = dbName,
                    user = "",
                    password = "",
                    retentionPolicyConfiguration = AnalyticsConfiguration.Influx.RetentionPolicyConfiguration.default
                )
            ).createDb()
        }

        @BeforeAll
        @JvmStatic
        fun `start influx and insert test data`() {
            container.start()
            val tracker = InfluxDbTracker(influxDb, dbName, rpName)
            getTestEvents().forEach { tracker.track(it) }
            influxDb.flush()
        }

        @AfterAll
        @JvmStatic
        fun `stop influx`() {
            container.stop()
        }
    }
}
