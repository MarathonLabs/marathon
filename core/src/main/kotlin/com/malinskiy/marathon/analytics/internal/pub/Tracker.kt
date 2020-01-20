package com.malinskiy.marathon.analytics.internal.pub

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.test.Test
import java.time.Instant

/**
 * Interface for reporting events
 */
interface Tracker {
    fun deviceConnected(poolId: DevicePoolId, device: DeviceInfo)
    fun deviceProviderInit(serialNumber: String, startTime: Instant, finishTime: Instant)
    fun devicePreparing(serialNumber: String, startTime: Instant, finishTime: Instant)
    fun installationCheck(serialNumber: String, startTime: Instant, finishTime: Instant)
    fun installation(serialNumber: String, startTime: Instant, finishTime: Instant)
    fun executingBatch(serialNumber: String, startTime: Instant, finishTime: Instant)
    fun cacheStore(startTime: Instant, finishTime: Instant, test: Test)
    fun cacheLoad(startTime: Instant, finishTime: Instant, test: Test)

    /**
     * @param final signals if the test execution status is final, i.e. no more retries will happen
     *              reporters such as jUnit should handle this as an indication to save report
     */
    fun test(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult, final: Boolean)
}
