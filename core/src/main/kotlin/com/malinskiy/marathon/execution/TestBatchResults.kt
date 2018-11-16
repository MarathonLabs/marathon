package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.Test

data class TestBatchResults(val device: Device,
                            val finished: Collection<TestResult>,
                            val failed: Collection<TestResult>,
                            val uncompleted: Collection<Test>)
