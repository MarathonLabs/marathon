package com.malinskiy.marathon.analytics.internal.sub

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.test.Test

data class PoolSummary(
    val poolId: DevicePoolId,
    val tests: List<TestResult>,
    val retries: Map<TestResult, List<TestEvent>>,
    val passed: Set<Test>,
    val ignored: Set<Test>,
    val failed: Set<Test>,
    val flaky: Int,
    val durationMillis: Long,
    val devices: List<DeviceInfo>,
    val rawPassed: List<Test>,
    val rawIgnored: List<Test>,
    val rawFailed: List<Test>,
    val rawIncomplete: List<Test>,
    val rawDurationMillis: Long
)
