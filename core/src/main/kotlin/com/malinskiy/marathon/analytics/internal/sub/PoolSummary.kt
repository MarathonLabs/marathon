package com.malinskiy.marathon.analytics.internal.sub

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult

data class PoolSummary(
    val poolId: DevicePoolId,
    val tests: List<TestResult>,
    val passed: Set<String>,
    val ignored: Set<String>,
    val failed: Set<String>,
    val flaky: Int,
    val durationMillis: Long,
    val devices: List<DeviceInfo>,
    val rawPassed: Int,
    val rawIgnored: Int,
    val rawFailed: Int,
    val rawIncomplete: Int,
    val rawDurationMillis: Long
)
