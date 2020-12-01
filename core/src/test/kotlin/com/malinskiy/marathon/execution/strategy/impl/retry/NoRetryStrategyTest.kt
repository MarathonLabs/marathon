package com.malinskiy.marathon.execution.strategy.impl.retry

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.generateTestResults
import com.malinskiy.marathon.generateTests
import org.amshove.kluent.shouldBeEmpty
import org.junit.jupiter.api.Test

class NoRetryStrategyTest {
    @Test
    fun `should return empty list`() {
        val tests = generateTests(50)
        val testResults = generateTestResults(tests)
        val strategy = NoRetryStrategy()
        val devicePoolId = DevicePoolId("devicePoolId")
        val testShard = TestShard(tests)
        val result = strategy.process(devicePoolId, testResults, testShard)
        result.shouldBeEmpty()
    }
}
