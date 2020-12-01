package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.external.NoOpMetricsProvider
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.util.*

class FixedSizeBatchingStrategyTest {
    val analytics = Analytics(NoOpMetricsProvider())

    @Test
    fun `test batching strategy with fixed size should create 5 batches for 50 tests with batch size 10`() {
        val tests = LinkedList(generateTests(50))
        val strategy = FixedSizeBatchingStrategy(10)
        val batch = strategy.process(tests, analytics)
        batch.tests.size shouldBe 10
    }

    @Test
    fun `test batching strategy with fixed size should create 1 batch for 10 tests with batch size 10`() {
        val tests = LinkedList(generateTests(10))
        val strategy = FixedSizeBatchingStrategy(10)
        val batch = strategy.process(tests, analytics)
        batch.tests.size shouldBe 10
    }
}
