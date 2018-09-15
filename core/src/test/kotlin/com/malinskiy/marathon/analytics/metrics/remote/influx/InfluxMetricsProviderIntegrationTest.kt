package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.TestGenerator
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DeviceStub
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.toSafeTestName
import org.amshove.kluent.shouldEqualTo
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.junit.Before
import org.junit.Rule
import org.junit.Test as TestAnnotation
import org.testcontainers.containers.InfluxDBContainer
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import com.malinskiy.marathon.test.Test

class KInfluxDBContainer : InfluxDBContainer<KInfluxDBContainer>()

const val database = "testDb"

class InfluxMetricsProviderIntegrationTest {

    @get:Rule
    var influxDbContainer = KInfluxDBContainer().withAuthEnabled(false).withDatabase(database)

    private lateinit var influxDB: InfluxDB
    private lateinit var provider: InfluxMetricsProvider

    private val test = TestGenerator().create(1).first()
    private val device = DeviceStub()

    @Before
    fun setUp() {
        influxDB = influxDbContainer.newInfluxDB
        provider = InfluxMetricsProvider(influxDB, database)
    }

    data class TestData(val test: Test,
                        val device: DeviceInfo,
                        val status: TestStatus,
                        val duration: Long,
                        val whenWasSent: Instant) {
        val isIgnored get() = status == TestStatus.IGNORED
    }

    @TestAnnotation
    fun testSuccessRateValue_DbContains0ValidItems() {
        val test = TestGenerator().create(1).first()
        val result = provider.successRate(test, Instant.now())
        result shouldEqualTo 0.0
    }

    @TestAnnotation
    fun testExecutionTimeValue_DbContains0ValidItems() {
        val influxDB = influxDbContainer.newInfluxDB
        val provider = InfluxMetricsProvider(influxDB, database)
        val test = TestGenerator().create(1).first()
        val result = provider.executionTime(test, 90.0, Instant.now())
        result shouldEqualTo 0.0
    }

    @TestAnnotation
    fun testExecutionTimeValue_ForLast2Days50Percentile() {
        prepareData()
        val influxDB = influxDbContainer.newInfluxDB
        val provider = InfluxMetricsProvider(influxDB, database)
        val test = TestGenerator().create(1).first()
        val result = provider.executionTime(test, 50.0, Instant.now().minus(2, ChronoUnit.DAYS))
        result shouldEqualTo 5000.0
    }

    @TestAnnotation
    fun testExecutionTimeValue_ForLast2Days90Percentile() {
        prepareData()
        val influxDB = influxDbContainer.newInfluxDB
        val provider = InfluxMetricsProvider(influxDB, database)
        val test = TestGenerator().create(1).first()
        val result = provider.executionTime(test, 90.0, Instant.now().minus(2, ChronoUnit.DAYS))
        result shouldEqualTo 9000.0
    }

    fun prepareData() {
        val instant = Instant.now()
        val list = listOf(
                TestData(test = test,
                        device = device.toDeviceInfo(),
                        status = TestStatus.PASSED,
                        duration = 1_000,
                        whenWasSent = instant.minus(1, ChronoUnit.MINUTES)
                ),
                TestData(test = test,
                        device = device.toDeviceInfo(),
                        status = TestStatus.PASSED,
                        duration = 2_000,
                        whenWasSent = instant.minus(10, ChronoUnit.MINUTES)
                ),
                TestData(test = test,
                        device = device.toDeviceInfo(),
                        status = TestStatus.PASSED,
                        duration = 3_000,
                        whenWasSent = instant.minus(20, ChronoUnit.MINUTES)
                ),
                TestData(test = test,
                        device = device.toDeviceInfo(),
                        status = TestStatus.PASSED,
                        duration = 4_000,
                        whenWasSent = instant.minus(30, ChronoUnit.MINUTES)
                ),
                TestData(test = test,
                        device = device.toDeviceInfo(),
                        status = TestStatus.PASSED,
                        duration = 5_000,
                        whenWasSent = instant.minus(40, ChronoUnit.MINUTES)
                ),
                TestData(test = test,
                        device = device.toDeviceInfo(),
                        status = TestStatus.PASSED,
                        duration = 6_000,
                        whenWasSent = instant.minus(50, ChronoUnit.MINUTES)
                ),
                TestData(test = test,
                        device = device.toDeviceInfo(),
                        status = TestStatus.PASSED,
                        duration = 7_000,
                        whenWasSent = instant.minus(60, ChronoUnit.MINUTES)
                ),
                TestData(test = test,
                        device = device.toDeviceInfo(),
                        status = TestStatus.PASSED,
                        duration = 8_000,
                        whenWasSent = instant.minus(70, ChronoUnit.MINUTES)
                ),
                TestData(test = test,
                        device = device.toDeviceInfo(),
                        status = TestStatus.PASSED,
                        duration = 9_000,
                        whenWasSent = instant.minus(80, ChronoUnit.MINUTES)
                ),
                TestData(test = test,
                        device = device.toDeviceInfo(),
                        status = TestStatus.PASSED,
                        duration = 10_000,
                        whenWasSent = instant.minus(90, ChronoUnit.MINUTES)
                ))
        list.forEach {
            sendData(it)
        }
    }

    fun sendData(item: TestData) {
        influxDB.write(Point.measurement("tests")
                .time(item.whenWasSent.toEpochMilli(), TimeUnit.MILLISECONDS)
                .tag("testname", item.test.toSafeTestName())
                .tag("package", item.test.pkg)
                .tag("class", item.test.clazz)
                .tag("method", item.test.method)
                .tag("deviceSerial", device.serialNumber)
                .addField("ignored", if (item.isIgnored) 1.0 else 0.0)
                .addField("success", if (item.status == TestStatus.PASSED) 1.0 else 0.0)
                .addField("duration", item.duration)
                .build())
    }

}
