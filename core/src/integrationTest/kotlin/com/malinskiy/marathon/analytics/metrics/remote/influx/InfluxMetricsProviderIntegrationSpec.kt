package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.device.DeviceStub
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.generateTest
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName
import org.amshove.kluent.shouldBeInRange
import org.amshove.kluent.shouldEqualTo
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.api.lifecycle.CachingMode
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class InfluxMetricsProviderIntegrationSpec : Spek({

    val database = "marathonDb"

    val container = KInfluxDBContainer().withAuthEnabled(false).withDatabase(database)

    beforeGroup {
        container.start()
    }
    afterGroup {
        container.stop()
    }

    val influxDB = memoized(mode = CachingMode.GROUP) {
        container.newInfluxDB
    }

    val dataStore = memoized(mode = CachingMode.TEST){
        InfluxDBDataSource(influxDB.invoke(), database)
    }

    val provider = memoized(mode = CachingMode.TEST) {
        InfluxMetricsProvider(dataStore.invoke())
    }

    describe("InfluxMetricsProvider") {
        val test = generateTest()
        on("empty db") {
            it("success rate default value is 0.0") {
                val result = provider.invoke().successRate(test, Instant.now())
                result shouldEqualTo 0.0
            }
            it("execution time default value is 300_000.0") {
                val result = provider.invoke().executionTime(test, 90.0, Instant.now())
                result shouldEqualTo 300_000.0
            }
        }
        group("execution time") {
            beforeGroup {
                prepareData(influxDB.invoke(), test)
            }
            it("50 percentile for last two days") {
                val result = provider.invoke().executionTime(test, 50.0, Instant.now().minus(2, ChronoUnit.DAYS))
                result shouldEqualTo 5000.0
            }
            it("90 percentile for last two days") {
                val result = provider.invoke().executionTime(test, 90.0, Instant.now().minus(2, ChronoUnit.DAYS))
                result shouldEqualTo 9000.0
            }
            it("50 percentile for 25 minutes") {
                val result = provider.invoke().executionTime(test, 50.0, Instant.now().minus(25, ChronoUnit.MINUTES))
                result shouldEqualTo 2000.0
            }
            it("90 percentile for 25 minutes") {
                val result = provider.invoke().executionTime(test, 90.0, Instant.now().minus(35, ChronoUnit.MINUTES))
                result shouldEqualTo 4000.0
            }
        }
        group("test success rate") {
            beforeGroup {
                prepareData(influxDB.invoke(), test)
            }
            it("should return 1.0 for last 50 minutes") {
                val result = provider.invoke().successRate(test, Instant.now().minus(50, ChronoUnit.MINUTES))
                result shouldEqualTo 1.0
            }
            it("should return 0.7 for last 70 minutes") {
                val result = provider.invoke().successRate(test, Instant.now().minus(70, ChronoUnit.MINUTES))
                result.shouldBeInRange(0.833, 0.834)
            }
            it("should return 0.5 for last 2 days") {
                val result = provider.invoke().successRate(test, Instant.now().minus(2, ChronoUnit.DAYS))
                result shouldEqualTo 0.5
            }
        }
    }
})

fun prepareData(influxDb: InfluxDB, test: Test) {
    val instant = Instant.now()
    val device = DeviceStub()
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
                    status = TestStatus.FAILURE,
                    duration = 6_000,
                    whenWasSent = instant.minus(60, ChronoUnit.MINUTES)
            ),
            TestData(test = test,
                    device = device.toDeviceInfo(),
                    status = TestStatus.FAILURE,
                    duration = 7_000,
                    whenWasSent = instant.minus(70, ChronoUnit.MINUTES)
            ),
            TestData(test = test,
                    device = device.toDeviceInfo(),
                    status = TestStatus.FAILURE,
                    duration = 8_000,
                    whenWasSent = instant.minus(80, ChronoUnit.MINUTES)
            ),
            TestData(test = test,
                    device = device.toDeviceInfo(),
                    status = TestStatus.FAILURE,
                    duration = 9_000,
                    whenWasSent = instant.minus(90, ChronoUnit.MINUTES)
            ),
            TestData(test = test,
                    device = device.toDeviceInfo(),
                    status = TestStatus.FAILURE,
                    duration = 10_000,
                    whenWasSent = instant.minus(100, ChronoUnit.MINUTES)
            ))
    list.forEach {
        sendData(it, influxDb)
    }
}

fun sendData(item: TestData, influxDB: InfluxDB) {
    influxDB.write(Point.measurement("tests")
            .time(item.whenWasSent.toEpochMilli(), TimeUnit.MILLISECONDS)
            .tag("testname", item.test.toSafeTestName())
            .tag("package", item.test.pkg)
            .tag("class", item.test.clazz)
            .tag("method", item.test.method)
            .tag("deviceSerial", item.device.serialNumber)
            .addField("ignored", if (item.isIgnored) 1.0 else 0.0)
            .addField("success", if (item.status == TestStatus.PASSED) 1.0 else 0.0)
            .addField("duration", item.duration)
            .build())
}
