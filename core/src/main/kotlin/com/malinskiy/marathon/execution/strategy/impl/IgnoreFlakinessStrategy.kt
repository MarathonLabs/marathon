package com.malinskiy.marathon.execution.strategy.impl

import com.malinskiy.marathon.execution.ExecutionShard
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy

class IgnoreFlakinessStrategy : FlakinessStrategy {
    override fun process(executionShards: Collection<ExecutionShard>): Collection<ExecutionShard> = executionShards
}