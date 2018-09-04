package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.test.Test

data class TestResult(val test: Test,
                      val device: DeviceInfo,
                      val status: TestStatus,
                      val startTime: Long,
                      val endTime: Long,
                      val stacktrace: String? = null) {
    fun durationMillis() = endTime - startTime

    val isIgnored: Boolean
        get() = when (status) {
            TestStatus.IGNORED, TestStatus.ASSUMPTION_FAILURE -> true
            else -> false
        }

    val isSuccess: Boolean
        get() = when (status) {
            TestStatus.PASSED -> true
            else -> false
        }
}
