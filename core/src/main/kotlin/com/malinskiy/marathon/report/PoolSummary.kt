package com.malinskiy.marathon.report

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult

data class PoolSummary(val poolId: DevicePoolId,
                       val tests: List<TestResult>,
                       val passed: Int,
                       val ignored: Int,
                       val failed: Int,
                       val flaky: Int,
                       val durationMillis: Long,
                       val devices: List<Device>)
