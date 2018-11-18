package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.TestGenerator
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.analytics.metrics.NoOpMetricsProvider
import com.malinskiy.marathon.analytics.tracker.NoOpTracker
import com.malinskiy.marathon.test.Test
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.*

class IsolateBatchingStrategySpek : Spek({

    val analytics = Analytics(NoOpTracker(), NoOpMetricsProvider())

    describe("isolate batching strategy test") {
        it("should return batches with size = 1") {
            val strategy = IsolateBatchingStrategy()
            val queue = LinkedList<Test>()
            val tests = TestGenerator().create(50)
            queue.addAll(tests)
            queue.size shouldBe 50
            strategy.process(queue, analytics).tests.size shouldBe 1
            queue.size shouldBe 49
            strategy.process(queue, analytics).tests.size shouldBe 1
            queue.size shouldBe 48
            strategy.process(queue, analytics).tests.size shouldBe 1
            queue.size shouldBe 47
        }
    }
})
