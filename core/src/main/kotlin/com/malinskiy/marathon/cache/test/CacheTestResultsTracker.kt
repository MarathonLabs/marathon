package com.malinskiy.marathon.cache.test

import com.malinskiy.marathon.analytics.internal.pub.Tracker
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import java.time.Instant

class CacheTestResultsTracker(private val testCacheSaver: TestCacheSaver) : Tracker {

    override fun test(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult, final: Boolean) {
        if (!testResult.isFromCache && final && testResult.isSuccess) {
            testCacheSaver.saveTestResult(poolId, testResult)
        }
    }

    override fun deviceConnected(poolId: DevicePoolId, device: DeviceInfo) {
        // no-op
    }

    override fun deviceProviderInit(serialNumber: String, startTime: Instant, finishTime: Instant) {
        // no-op
    }

    override fun devicePreparing(serialNumber: String, startTime: Instant, finishTime: Instant) {
        // no-op
    }

    override fun close() {
        // no-op
    }
}