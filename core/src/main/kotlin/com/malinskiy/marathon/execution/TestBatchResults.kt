package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device

data class TestBatchResults(val device: Device,
                            val passed: Collection<TestResult>,
                            val failed: Collection<TestResult>,
                            val incomplete: Collection<TestResult>,
                            val missed: Collection<TestResult>)
