package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.execution.ExecutionShard

interface SortingStrategy {
    fun process(executionShards: Collection<ExecutionShard>): Collection<ExecutionShard>
}