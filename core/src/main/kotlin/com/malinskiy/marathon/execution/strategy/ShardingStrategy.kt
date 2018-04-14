package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.device.DevicePool
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.test.Test

interface ShardingStrategy {
    fun createShards(tests: Collection<Test>, pool: DevicePool): Collection<TestShard>
}