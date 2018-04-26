package com.malinskiy.marathon.execution.strategy.impl.sorting

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.SortingStrategy

class NoSortingStrategy : SortingStrategy {
    override fun process(testShard: TestShard): TestShard = testShard
}