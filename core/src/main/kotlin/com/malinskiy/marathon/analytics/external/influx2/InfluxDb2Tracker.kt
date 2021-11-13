package com.malinskiy.marathon.analytics.external.influx2

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternalAdapter
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.toSafeTestName

class InfluxDb2Tracker(
    private val client: InfluxDBClient
) : TrackerInternalAdapter() {
    private val writeApi by lazy { client.writeApiBlocking }

    override fun trackTest(event: TestEvent) {
        //Report only success and failure
        if (event.testResult.status in arrayOf(TestStatus.FAILURE, TestStatus.PASSED)) {

            val testResult = event.testResult
            val device = event.device

            writeApi.writePoint(
                Point("tests")
                    .time(event.instant, WritePrecision.MS)
                    .addTag("testname", testResult.test.toSafeTestName())
                    .addTag("package", testResult.test.pkg)
                    .addTag("class", testResult.test.clazz)
                    .addTag("method", testResult.test.method)
                    .addTag("deviceSerial", device.safeSerialNumber)
                    .addField("ignored", if (testResult.isIgnored) 1.0 else 0.0)
                    .addField("success", if (testResult.status == TestStatus.PASSED) 1.0 else 0.0)
                    .addField("duration", testResult.durationMillis().toDouble()) //Percentiles are not defined for Ints in InfluxDb2
            )
        }
    }

    override fun close() {
        super.close()
        client.close()
    }
}
