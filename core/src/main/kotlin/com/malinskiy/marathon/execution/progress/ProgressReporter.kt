package com.malinskiy.marathon.execution.progress

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.test.Test

class ProgressReporter {
    private val reporters = mutableMapOf<DevicePoolId, PoolProgressTracker>()

    private inline fun execute(poolId: DevicePoolId, f: (PoolProgressTracker) -> Unit) {
        val reporter = reporters[poolId] ?: PoolProgressTracker()
        f(reporter)
        reporters[poolId] = reporter
    }

    fun testStarted(poolId: DevicePoolId, test: Test) {

        execute(poolId) { it.testStarted(test) }
    }

    fun testFailed(poolId: DevicePoolId, test: Test) {
        execute(poolId) { it.testFailed(test) }
    }

    fun testEnded(poolId: DevicePoolId, test: Test) {
        execute(poolId) { it.testEnded(test) }
    }

    fun testIgnored(poolId: DevicePoolId, test: Test) {
        execute(poolId) { it.testIgnored(test) }
    }

    fun aggregateResult(): Boolean = reporters.values.all {
        it.aggregateResult()
    }
}