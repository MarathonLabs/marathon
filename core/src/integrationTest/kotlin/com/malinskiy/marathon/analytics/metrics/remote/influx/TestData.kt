package com.malinskiy.marathon.analytics.metrics.remote.influx

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test
import java.time.Instant

data class TestData(val test: Test,
                    val device: DeviceInfo,
                    val status: TestStatus,
                    val duration: Long,
                    val whenWasSent: Instant) {
    val isIgnored
        get() = when (status) {
            TestStatus.IGNORED, TestStatus.ASSUMPTION_FAILURE -> true
            else -> false
        }

}
