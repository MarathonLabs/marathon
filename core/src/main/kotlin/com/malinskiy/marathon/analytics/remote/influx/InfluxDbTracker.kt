package com.malinskiy.marathon.analytics.remote.influx

import com.malinskiy.marathon.analytics.NoOpTracker
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.report.Status
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName
import org.influxdb.BatchOptions
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Point
import org.influxdb.impl.InfluxDBResultMapper
import java.util.concurrent.TimeUnit

class InfluxDbTracker(private val influxDb: InfluxDB) : NoOpTracker() {
    override fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult) {
        influxDb.write(Point.measurement("tests")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("testname", testResult.test.toSafeTestName())
                .addField("success", if (testResult.status == TestStatus.PASSED) 1.0 else 0.0)
                .addField("duration", testResult.durationMillis())
                .build())
    }
}