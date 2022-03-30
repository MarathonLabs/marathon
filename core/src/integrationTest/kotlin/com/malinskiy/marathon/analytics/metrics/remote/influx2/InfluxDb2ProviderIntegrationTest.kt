package com.malinskiy.marathon.analytics.metrics.remote.influx2

import com.influxdb.client.InfluxDBClient
import com.malinskiy.marathon.analytics.external.MetricsProviderImpl
import com.malinskiy.marathon.analytics.external.influx2.InfluxDb2Provider
import com.malinskiy.marathon.analytics.external.influx2.InfluxDb2Tracker
import com.malinskiy.marathon.analytics.metrics.remote.getTestEvents
import com.malinskiy.marathon.config.AnalyticsConfiguration
import com.malinskiy.marathon.generateTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class InfluxDb2ProviderIntegrationTest {
    private val username = "user"
    private val password = "password"
    private val token = "my-super-secret-auth-token"
    private val bucket = "marathonTest"
    private val org = "testOrg"

    private val container: InfluxDBContainerV2 =
        InfluxDBContainerV2()
            .withUsername(username).withPassword(password)
            .withAdminToken(token)
            .withOrganization(org).withBucket(bucket)

    var thirdDbInstance: InfluxDBClient? = null

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
        val provider = InfluxDb2Provider(
            AnalyticsConfiguration.InfluxDb2Configuration(
                url = container.url,
                token = token,
                bucket = bucket,
                organization = org,
            )
        )
        val firstDbInstance = provider.createDb()
        firstDbInstance.close()

        val secondDbInstance = provider.createDb()
        val tracker = InfluxDb2Tracker(secondDbInstance)
        val test1 = com.malinskiy.marathon.test.Test("pkg", "clazz", "method", emptyList())
        val test2 = com.malinskiy.marathon.test.Test("pkg", "clazz", "method2", emptyList())
        getTestEvents(test = test1).forEach { tracker.track(it) }
        getTestEvents(test = test2).forEach { tracker.track(it) }
        secondDbInstance.close()

        thirdDbInstance = provider.createDb()

        val metricsProvider =
            MetricsProviderImpl(InfluxDB2DataSource(thirdDbInstance!!, bucket))

        val result1 = metricsProvider.executionTime(
            test,
            50.0,
            Instant.now().minus(2, ChronoUnit.DAYS)
        )
        result1 shouldBeEqualTo 5000.0

        val result2 = metricsProvider.executionTime(
            test2,
            50.0,
            Instant.now().minus(2, ChronoUnit.DAYS)
        )
        result2 shouldBeEqualTo 5000.0
    }
}
