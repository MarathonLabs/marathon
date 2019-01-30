package com.malinskiy.marathon

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test

class TestResultsGenerator {
    fun create(tests: List<Test>): List<TestResult> {
        return tests.map {
            TestResult(it,
                    createDeviceInfo(),
                    TestStatus.PASSED,
                    0,
                    10000)
        }
    }
}
