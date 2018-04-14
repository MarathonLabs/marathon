package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.execution.TestShard

interface SortingStrategy {
    fun process(testShard: TestShard): TestShard
}