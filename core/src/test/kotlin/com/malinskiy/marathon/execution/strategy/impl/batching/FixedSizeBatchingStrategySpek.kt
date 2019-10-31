package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.external.NoOpMetricsProvider
import com.malinskiy.marathon.generateTests
import com.malinskiy.marathon.test.TestComponentInfo
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldContainSame
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.*

class FixedSizeBatchingStrategySpek : Spek(
    {

        val analytics = Analytics(NoOpMetricsProvider())

        describe("test batching strategy with fixed size") {
            it("should create 5 batches for 50 tests with batch size 10") {
                val tests = LinkedList(generateTests(50))
                val strategy = FixedSizeBatchingStrategy(10)
                val batch = strategy.process(tests, analytics)
                batch.tests.size shouldBe 10
            }
            it("should create 1 batch for 10 tests with batch size 10") {
                val tests = LinkedList(generateTests(10))
                val strategy = FixedSizeBatchingStrategy(10)
                val batch = strategy.process(tests, analytics)
                batch.tests.size shouldBe 10
            }
            it("should create 2 batches for 10 tests with batch size 10 and different component infos") {
                val componentInfo1 = TestComponentInfo("first")
                val componentInfo2 = TestComponentInfo("second")
                val tests = LinkedList(
                    generateTests(5, componentInfo = componentInfo1) + generateTests(5, componentInfo = componentInfo2)
                )
                val strategy = FixedSizeBatchingStrategy(10)
                val batch1 = strategy.process(tests, analytics)
                batch1.tests.size shouldBe 5
                batch1.tests.map { it.componentInfo }.toSet() shouldContainSame setOf(componentInfo1)
                val batch2 = strategy.process(tests, analytics)
                batch2.tests.size shouldBe 5
                batch2.tests.map { it.componentInfo }.toSet() shouldContainSame setOf(componentInfo2)
            }
        }
    })
