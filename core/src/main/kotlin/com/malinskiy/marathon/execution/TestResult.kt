package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.test.Test

data class TestResult(val test: Test,
                      val device: DeviceInfo,
                      val status: TestStatus,
                      val startTime: Long,
                      val endTime: Long,
                      val stacktrace: String? = null){
    fun durationMillis() = endTime - startTime
}
