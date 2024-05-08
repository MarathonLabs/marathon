package com.malinskiy.marathon.analytics.external.influx2

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.influxdb.exceptions.InfluxException
import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternalAdapter
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.toSafeTestName

class InfluxDb2Tracker(
    private val client: InfluxDBClient,
    private val readOnly: Boolean,
) : TrackerInternalAdapter() {
    private val writeApi by lazy { client.writeApiBlocking }
    private val logger = MarathonLogging.logger {}

    override fun trackTest(event: TestEvent) {
        if (readOnly) return

        val testResult = event.testResult
        val device = event.device
        try {
            writeApi.writePoint(
                Point("tests")
                    .time(event.instant, WritePrecision.MS)
                    .addTag("testname", testResult.test.toSafeTestName())
                    .addTag("package", testResult.test.pkg)
                    .addTag("class", testResult.test.clazz)
                    .addTag("method", testResult.test.method)
                    .addTag("deviceSerial", device.safeSerialNumber)
                    .addField("ignored", if (testResult.isIgnored) 1.0 else 0.0)
                    .apply {
                        /**
                         * For calculation of a success of a test we treat only failure as 0
                         * Passed/Ignored/AssumptionFailure are terminal states that guarantee that the test finishes in one pass
                         * Incomplete should not be reported since it is unclear if it's a failure of a device or a test failure
                         */
                        val value = when (event.testResult.status) {
                            TestStatus.FAILURE -> {
                                0.0
                            }

                            TestStatus.PASSED, TestStatus.IGNORED, TestStatus.ASSUMPTION_FAILURE -> 1.0
                            TestStatus.INCOMPLETE -> null
                        }
                        value?.let {
                            addField("success", it)
                        }
                    }
                    /**
                     * Percentiles are not defined for int in InfluxDb2 -> convert to floating point
                     */
                    .addField("duration", testResult.durationMillis().toDouble())
            )
        } catch (e: InfluxException) {
            logger.error(e) { "Failed to write data point to InfluxDb2" }
        }
    }

    override fun close() {
        super.close()
        client.close()
    }
}
