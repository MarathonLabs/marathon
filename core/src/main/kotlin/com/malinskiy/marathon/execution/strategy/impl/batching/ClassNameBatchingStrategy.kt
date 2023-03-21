package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import java.util.Queue

class ClassNameBatchingStrategy(private val cnf: BatchingStrategyConfiguration.ClassNameBatchingStrategyConfiguration) : BatchingStrategy {

    override fun process(queue: Queue<Test>, analytics: Analytics, testBundleIdentifier: TestBundleIdentifier?): TestBatch {
        val firstGroup = queue.toList().groupBy { test -> "${test.pkg}.${test.clazz}" }.values.first()
        queue.removeAll(firstGroup.toSet())
        return TestBatch(firstGroup)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassNameBatchingStrategy

        return true
    }

    override fun hashCode(): Int {
        return javaClass.canonicalName.hashCode()
    }

    override fun toString(): String {
        return "ClassNameBatchingStrategy()"
    }
}
