package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.DevicePool

data class ExecutionPool(
    val devicePool: DevicePool,
    val testShard: TestShard
)