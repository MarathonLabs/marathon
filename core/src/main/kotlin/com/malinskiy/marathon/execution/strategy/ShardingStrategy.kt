package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.device.DevicePool
import com.malinskiy.marathon.execution.ExecutionShard
import com.malinskiy.marathon.test.Test

interface ShardingStrategy {
    fun createShards(tests: Collection<Test>, pools: Collection<DevicePool>) : Collection<ExecutionShard>
}