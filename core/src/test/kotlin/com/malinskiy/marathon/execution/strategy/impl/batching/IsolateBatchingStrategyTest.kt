package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.external.NoOpMetricsProvider
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.Test
import java.util.*
import com.malinskiy.marathon.test.Test as MarathonTest

class IsolateBatchingStrategyTest {
    val analytics = Analytics(NoOpMetricsProvider())

    @Test
    fun `isolate batching strategy test should return batches with size = 1`() {
        val strategy = IsolateBatchingStrategy()
        val queue = LinkedList<MarathonTest>()
        val tests = generateTests(50)
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
