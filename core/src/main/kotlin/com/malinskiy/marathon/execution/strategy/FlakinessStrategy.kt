package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.execution.TestShard

interface FlakinessStrategy {
    fun process(testShard: TestShard): TestShard
}
