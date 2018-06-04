package com.malinskiy.marathon.analytics.remote.influx

import com.malinskiy.marathon.analytics.NoOpTracker
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.report.Status
import com.malinskiy.marathon.test.Test
import org.influxdb.BatchOptions
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import org.influxdb.impl.InfluxDBResultMapper
import java.util.concurrent.TimeUnit

private const val dbName = "tests"

class InfluxDbTracker : NoOpTracker(), AutoCloseable {

    private val influxDb = InfluxDBFactory.connect("http://localhost:8086", "root", "root")

    init {
        if (!influxDb.databaseExists(dbName)) {
            influxDb.createDatabase(dbName)
        }
        influxDb.setDatabase(dbName)
        val rpName = "aRetentionPolicy"
        influxDb.createRetentionPolicy(rpName, dbName, "30d", "30m", 2, true)
        influxDb.setRetentionPolicy(rpName)

        influxDb.enableBatch(BatchOptions.DEFAULTS)
    }

    override fun close() {
        println("InfixDbTracker close")
        influxDb.close()
    }

    override fun trackTestStarted(test: Test, time: Int) {
        super.trackTestStarted(test, time)
    }

    override fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult) {
        val test = testResult.test
        val testName = "${test.pkg}.${test.clazz}.${test.method}"
        influxDb.write(Point.measurement("tests")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("testname", testName)
                .addField("success", if (testResult.status == Status.Passed) 1 else 0)
                .build())
    }

    override fun trackTestIgnored(test: Test) {
        super.trackTestIgnored(test)
    }
}