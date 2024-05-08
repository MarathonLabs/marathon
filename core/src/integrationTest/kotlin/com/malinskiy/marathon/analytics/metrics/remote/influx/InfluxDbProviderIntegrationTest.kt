package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.external.MetricsProviderImpl
import com.malinskiy.marathon.analytics.external.influx.InfluxDbProvider
import com.malinskiy.marathon.analytics.external.influx.InfluxDbTracker
import com.malinskiy.marathon.analytics.metrics.remote.getTestEvents
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.generateTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldEqualTo
import org.influxdb.InfluxDB
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

class InfluxDbProviderIntegrationTest {
    private val database = "marathonTest"
    private val rpName = "rpMarathon"

    private val container: InfluxV1Container = InfluxV1Container().withAuthEnabled(false)

    var thirdDbInstance: InfluxDB? = null

    @BeforeEach
    fun `start influx`() {
        container.start()
    }

    @AfterEach
    fun `stop influx`() {
        thirdDbInstance?.close()
        container.stop()
    }

    @Test
    fun `multiple creations should still have the same configured retention policy`() {
        val test = generateTest()
        val provider = InfluxDbProvider(
            AnalyticsConfiguration.InfluxDbConfiguration(
                url = container.url,
                dbName = database,
                password = "",
                user = "root",
                retentionPolicyConfiguration = AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
            )
        )
        val firstDbInstance = provider.createDb()
        firstDbInstance.close()


        val secondDbInstance = provider.createDb()
        val tracker = InfluxDbTracker(secondDbInstance, database, rpName, readOnly = false)
        getTestEvents().forEach { tracker.track(it) }
        secondDbInstance.close()

        thirdDbInstance = provider.createDb()

        val metricsProvider =
            MetricsProviderImpl(InfluxDBDataSource(thirdDbInstance!!, database, rpName), .0, Duration.ofMinutes(5))

        val result = metricsProvider.executionTime(
            test,
            50.0,
            Instant.now().minus(2, ChronoUnit.DAYS)
        )
        result shouldBeEqualTo 5000.0
    }
}
