package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.execution.ExecutionShard

interface FlakinessStrategy {
    fun process(executionShards: Collection<ExecutionShard>): Collection<ExecutionShard>
}