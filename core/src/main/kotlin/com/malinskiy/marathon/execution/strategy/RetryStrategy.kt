package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard

interface RetryStrategy {
    fun process(devicePoolId: DevicePoolId, tests: Collection<TestResult>, testShard: TestShard): List<TestResult>
}
