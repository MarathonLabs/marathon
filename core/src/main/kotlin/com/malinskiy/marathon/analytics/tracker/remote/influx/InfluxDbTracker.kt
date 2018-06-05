package com.malinskiy.marathon.analytics.tracker.remote.influx

import com.malinskiy.marathon.analytics.tracker.NoOpTracker
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.toSafeTestName
import org.influxdb.InfluxDB
import org.influxdb.dto.Point
import java.util.concurrent.TimeUnit

internal class InfluxDbTracker(private val influxDb: InfluxDB,
                               private val dbName: String) : NoOpTracker() {
    override fun trackTestResult(poolId: DevicePoolId, device: Device, testResult: TestResult) {
        influxDb.write(Point.measurement(dbName)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("testname", testResult.test.toSafeTestName())
                .addField("success", if (testResult.status == TestStatus.PASSED) 1.0 else 0.0)
                .addField("duration", testResult.durationMillis())
                .build())
    }
}