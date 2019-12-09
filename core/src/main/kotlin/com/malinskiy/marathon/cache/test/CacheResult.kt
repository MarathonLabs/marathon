package com.malinskiy.marathon.cache.test

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard

sealed class CacheResult {

    class Hit(
        val pool: DevicePoolId,
        val testResult: TestResult
    ) : CacheResult()

    class Miss(
        val pool: DevicePoolId,
        val testShard: TestShard
    ) : CacheResult()

}
