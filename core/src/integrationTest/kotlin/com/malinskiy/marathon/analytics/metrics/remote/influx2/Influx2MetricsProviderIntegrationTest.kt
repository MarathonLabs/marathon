package com.malinskiy.marathon.analytics.metrics.remote.influx2

import com.influxdb.client.InfluxDBClient
import com.malinskiy.marathon.analytics.external.influx2.InfluxDb2Provider
import com.malinskiy.marathon.analytics.external.influx2.InfluxDb2Tracker
import com.malinskiy.marathon.analytics.metrics.remote.BaseMetricsProviderIntegrationTest
import com.malinskiy.marathon.analytics.metrics.remote.getTestEvents
import com.malinskiy.marathon.config.AnalyticsConfiguration
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

class Influx2MetricsProviderIntegrationTest : BaseMetricsProviderIntegrationTest() {

    override fun createRemoteDataSource() = InfluxDB2DataSource(influxDb, bucket)

    private companion object {
        const val username = "user"
        const val password = "password"
        const val token = "my-super-secret-auth-token"
        const val bucket = "marathonTest"
        const val org = "testOrg"

        val container: InfluxDBContainerV2 =
            InfluxDBContainerV2()
                .withUsername(username).withPassword(password)
                .withAdminToken(token)
                .withOrganization(org).withBucket(bucket)

        val influxDb: InfluxDBClient by lazy {
            InfluxDb2Provider(
                AnalyticsConfiguration.InfluxDb2Configuration(
                    url = container.url,
                    token = token,
                    bucket = bucket,
                    organization = org,
                )
            ).createDb()
        }

        @BeforeAll
        @JvmStatic
        fun `start influx and insert test data`() {
            container.start()
            val tracker = InfluxDb2Tracker(influxDb, readOnly = false)
            getTestEvents().forEach { tracker.track(it) }
        }

        @AfterAll
        @JvmStatic
        fun `stop influx`() {
            container.stop()
        }
    }
}
