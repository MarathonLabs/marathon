package com.malinskiy.marathon.analytics.external.graphite

import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternalAdapter
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toSafeTestName

class GraphiteTracker(
    private val graphite: GraphiteClient
) : TrackerInternalAdapter() {

    override fun trackTest(event: TestEvent) {
        if (event.testResult.status in arrayOf(TestStatus.FAILURE, TestStatus.PASSED)) {
            val testResult = event.testResult
            val device = event.device

            val baseMetricName = getBaseMetricName(testResult.test, device)
            val timestamp = event.instant.toEpochMilli()

            graphite.send(
                listOf(
                    GraphiteMetric(
                        "$baseMetricName.ignored",
                        if (testResult.isIgnored) "1" else "0",
                        timestamp
                    ),
                    GraphiteMetric(
                        "$baseMetricName.success",
                        if (testResult.status == TestStatus.PASSED) "1" else "0",
                        timestamp
                    ),
                    GraphiteMetric(
                        "$baseMetricName.duration",
                        testResult.durationMillis().toString(),
                        timestamp
                    )
                )
            )
        }
    }

    private fun getBaseMetricName(test: Test, device: DeviceInfo): String {
        val testName = test.toSafeTestName().replaceDots()
        val testPackage = test.pkg.replaceDots()
        return "tests.$testName.$testPackage.${test.clazz}.${test.method}.${device.safeSerialNumber}"
    }

    private fun String.replaceDots() = replace(".", "--")
}
