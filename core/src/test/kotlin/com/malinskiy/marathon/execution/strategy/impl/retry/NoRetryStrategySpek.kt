package com.malinskiy.marathon.execution.strategy.impl.retry

import com.malinskiy.marathon.TestGenerator
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestShard
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEmpty
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class NoRetryStrategySpek : Spek({
    describe("no retry strategy test") {
        it("should return empty list") {
            val tests = TestGenerator().create(50)
            val strategy = NoRetryStrategy()
            val devicePoolId = DevicePoolId("devicePoolId")
            val testShard = TestShard(tests)
            val result = strategy.process(devicePoolId, tests, testShard)
            result.shouldBeEmpty()
        }
    }
})