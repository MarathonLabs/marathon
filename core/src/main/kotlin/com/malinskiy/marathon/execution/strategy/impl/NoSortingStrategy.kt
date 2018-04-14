package com.malinskiy.marathon.execution.strategy.impl

import com.malinskiy.marathon.execution.ExecutionShard
import com.malinskiy.marathon.execution.strategy.SortingStrategy

class NoSortingStrategy : SortingStrategy {
    override fun process(executionShards: Collection<ExecutionShard>): Collection<ExecutionShard> = executionShards
}