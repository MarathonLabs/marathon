package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.test.Test

interface RetryStrategy {
    fun process(devicePoolId: DevicePoolId, tests: Collection<Test>, testShard: TestShard): List<Test>
}
