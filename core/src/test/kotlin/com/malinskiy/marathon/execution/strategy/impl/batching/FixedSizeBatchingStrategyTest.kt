package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.external.NoOpMetricsProvider
import com.malinskiy.marathon.config.strategy.BatchingStrategyConfiguration
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.extension.toBatchingStrategy
import com.malinskiy.marathon.generateTests
import com.malinskiy.marathon.test.StubTestBundle
import com.malinskiy.marathon.test.StubTestBundleIdentifier
import org.amshove.kluent.`should contain same`
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.util.LinkedList

class FixedSizeBatchingStrategyTest {
    val analytics = Analytics(NoOpMetricsProvider())

    @Test
    fun `test batching strategy with fixed size should create 5 batches for 50 tests with batch size 10`() {
        val tests = LinkedList(generateTests(50))
        val strategy = BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration(10).toBatchingStrategy()
        val batch = strategy.process(tests, analytics, null)
        batch.tests.size shouldBe 10
    }

    @Test
    fun `test batching strategy with fixed size should create 1 batch for 10 tests with batch size 10`() {
        val tests = LinkedList(generateTests(10))
        val strategy = BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration(10).toBatchingStrategy()
        val batch = strategy.process(tests, analytics, null)
        batch.tests.size shouldBe 10
    }
    
    @Test
    fun `test batching should not place tests from different bundles in the same batch`() {
        val tests = LinkedList(generateTests(2))
        val strategy = BatchingStrategyConfiguration.FixedSizeBatchingStrategyConfiguration(2).toBatchingStrategy()
        val bundle1 = StubTestBundle("1")
        val bundle2 = StubTestBundle("2")

        val bundleIdentifier = StubTestBundleIdentifier().apply { 
            put(tests[0], bundle1)
            put(tests[1], bundle2)
        }
        val batch = strategy.process(tests, analytics, bundleIdentifier)
        batch.tests.size shouldBe 1
    }
}
