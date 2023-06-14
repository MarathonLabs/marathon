package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.external.NoOpMetricsProvider
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.extension.toBatchingStrategy
import com.malinskiy.marathon.generateClassGroupTests
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.util.LinkedList

class ClassNameBatchingStrategyTest {
    val analytics = Analytics(NoOpMetricsProvider())

    @Test
    fun `test class named strategy should create 10 batches for 10 classes with batch size 5`() {
        val tests = LinkedList(generateClassGroupTests(10, 5))
        val strategy = BatchingStrategyConfiguration.ClassNameBatchingStrategyConfiguration.toBatchingStrategy()
        val batch = strategy.process(tests, analytics, null)
        batch.tests.size shouldBe 5
    }

    @Test
    fun `test class named strategy should create 1 batch for 1 classes with batch size 5`() {
        val tests = LinkedList(generateClassGroupTests(1, 5))
        val strategy = BatchingStrategyConfiguration.ClassNameBatchingStrategyConfiguration.toBatchingStrategy()
        val batch = strategy.process(tests, analytics, null)
        batch.tests.size shouldBe 5
    }
}
