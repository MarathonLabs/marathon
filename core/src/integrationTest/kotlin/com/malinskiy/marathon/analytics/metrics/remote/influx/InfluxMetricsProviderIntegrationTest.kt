package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.analytics.external.influx.InfluxDbProvider
import com.malinskiy.marathon.analytics.external.influx.InfluxMetricsProvider
import com.malinskiy.marathon.device.DeviceStub
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.generateTest
import com.malinskiy.marathon.test.toSafeTestName
import org.amshove.kluent.shouldBeInRange
import org.amshove.kluent.shouldEqualTo
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import com.malinskiy.marathon.test.Test as MarathonTest

class InfluxMetricsProviderIntegrationTest {

    companion object {
        val container = KInfluxDBContainer().withAuthEnabled(false)
        val database = "marathonDb"
        val rpName = "rpMarathon"
        val test = generateTest()

        val influxDB = {
            val configuration = AnalyticsConfiguration.InfluxDbConfiguration(
                url = container.url,
                dbName = database,
                user = "",
                password = "",
                retentionPolicyConfiguration = AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
            )
            InfluxDbProvider(configuration).createDb()
        }

        @BeforeAll
        @JvmStatic
        fun `start influx`() {
            container.start()
            prepareData(influxDB.invoke(), test, database, rpName)
        }

        @AfterAll
        @JvmStatic
        fun `stop influx`() {
            container.stop()
        }
    }

    lateinit var dataStore: InfluxDBDataSource
    lateinit var provider: InfluxMetricsProvider

    @BeforeEach
    fun setup() {
        dataStore = InfluxDBDataSource(influxDB.invoke(), database, rpName)
        provider = InfluxMetricsProvider(dataStore)
    }

    @Test
    fun `on empty db success rate default value is 0`() {
        val result = provider.successRate(test, Instant.now())
        result shouldEqualTo 0.0
    }

    @Test
    fun `execution time default value is 300_000`() {
        val result = provider.executionTime(test, 90.0, Instant.now())
        result shouldEqualTo 300_000.0
    }

    @Test
    fun `verify execution time 50 percentile for last two days`() {
        val result = provider.executionTime(test, 50.0, Instant.now().minus(2, ChronoUnit.DAYS))
        result shouldEqualTo 5000.0
    }

    @Test
    fun `verify execution time 90 percentile for last two days`() {
        val result = provider.executionTime(test, 90.0, Instant.now().minus(2, ChronoUnit.DAYS))
        result shouldEqualTo 9000.0
    }

    @Test
    fun `verify execution time 50 percentile for 25 minutes`() {
        val result = provider.executionTime(
            test,
            50.0,
            Instant.now().minus(25, ChronoUnit.MINUTES)
        )
        result shouldEqualTo 2000.0
    }

    @Test
    fun `verify execution time 90 percentile for 25 minutes`() {
        val result = provider.executionTime(
            test,
            90.0,
            Instant.now().minus(35, ChronoUnit.MINUTES)
        )
        result shouldEqualTo 4000.0
    }

    @Test
    fun `verify test success rate should return 1 for last 50 minutes`() {
        val result = provider.successRate(test, Instant.now().minus(50, ChronoUnit.MINUTES))
        result shouldEqualTo 1.0
    }

    @Test
    fun `verify test success rate should return 0_833 for last 70 minutes`() {
        val result = provider.successRate(test, Instant.now().minus(70, ChronoUnit.MINUTES))
        result.shouldBeInRange(0.833, 0.834)
    }

    @Test
    fun `verify test success rate should return 0_5 for last 2 days`() {
        val result = provider.successRate(test, Instant.now().minus(2, ChronoUnit.DAYS))
        result shouldEqualTo 0.5
    }
}

fun prepareData(influxDb: InfluxDB, test: MarathonTest, database: String, rpName: String) {
    val instant = Instant.now()
    val device = DeviceStub()
    val list = listOf(
        TestData(
            test = test,
            device = device.toDeviceInfo(),
            status = TestStatus.PASSED,
            duration = 1_000,
            whenWasSent = instant.minus(1, ChronoUnit.MINUTES)
        ),
        TestData(
            test = test,
            device = device.toDeviceInfo(),
            status = TestStatus.PASSED,
            duration = 2_000,
            whenWasSent = instant.minus(10, ChronoUnit.MINUTES)
        ),
        TestData(
            test = test,
            device = device.toDeviceInfo(),
            status = TestStatus.PASSED,
            duration = 3_000,
            whenWasSent = instant.minus(20, ChronoUnit.MINUTES)
        ),
        TestData(
            test = test,
            device = device.toDeviceInfo(),
            status = TestStatus.PASSED,
            duration = 4_000,
            whenWasSent = instant.minus(30, ChronoUnit.MINUTES)
        ),
        TestData(
            test = test,
            device = device.toDeviceInfo(),
            status = TestStatus.PASSED,
            duration = 5_000,
            whenWasSent = instant.minus(40, ChronoUnit.MINUTES)
        ),
        TestData(
            test = test,
            device = device.toDeviceInfo(),
            status = TestStatus.FAILURE,
            duration = 6_000,
            whenWasSent = instant.minus(60, ChronoUnit.MINUTES)
        ),
        TestData(
            test = test,
            device = device.toDeviceInfo(),
            status = TestStatus.FAILURE,
            duration = 7_000,
            whenWasSent = instant.minus(70, ChronoUnit.MINUTES)
        ),
        TestData(
            test = test,
            device = device.toDeviceInfo(),
            status = TestStatus.FAILURE,
            duration = 8_000,
            whenWasSent = instant.minus(80, ChronoUnit.MINUTES)
        ),
        TestData(
            test = test,
            device = device.toDeviceInfo(),
            status = TestStatus.FAILURE,
            duration = 9_000,
            whenWasSent = instant.minus(90, ChronoUnit.MINUTES)
        ),
        TestData(
            test = test,
            device = device.toDeviceInfo(),
            status = TestStatus.FAILURE,
            duration = 10_000,
            whenWasSent = instant.minus(100, ChronoUnit.MINUTES)
        )
    )
    list.forEach {
        sendData(it, influxDb, database, rpName)
    }
    influxDb.flush()
}

fun sendData(
    item: TestData,
    influxDB: InfluxDB,
    database: String,
    rpName: String
) {
    influxDB.write(
        database, rpName,
        Point.measurement("tests")
            .time(item.whenWasSent.toEpochMilli(), TimeUnit.MILLISECONDS)
            .tag("testname", item.test.toSafeTestName())
            .tag("package", item.test.pkg)
            .tag("class", item.test.clazz)
            .tag("method", item.test.method)
            .tag("deviceSerial", item.device.serialNumber)
            .addField("ignored", if (item.isIgnored) 1.0 else 0.0)
            .addField("success", if (item.status == TestStatus.PASSED) 1.0 else 0.0)
            .addField("duration", item.duration)
            .build()
    )
}
