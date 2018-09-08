package com.malinskiy.marathon.execution.progress

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.progress.tracker.PoolProgressTracker
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

const val HUNDRED_PERCENT_IN_FLOAT: Float = 100.0f

class ProgressReporter {
    private val reporters = ConcurrentHashMap<DevicePoolId, PoolProgressTracker>()

    private inline fun <T> execute(poolId: DevicePoolId, f: (PoolProgressTracker) -> T): T {
        val reporter = reporters[poolId] ?: PoolProgressTracker()
        val result = f(reporter)
        reporters[poolId] = reporter
        return result
    }

    private fun toPercent(float: Float): String {
        val percent = (float * HUNDRED_PERCENT_IN_FLOAT).roundToInt()
        val format = "%02d%%"
        return String.format(format, percent)
    }

    fun testStarted(poolId: DevicePoolId, device: Device, test: Test) {
        execute(poolId) { it.testStarted(test, device) }
        println("${toPercent(progress(poolId))} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} started")
    }

    fun testFailed(poolId: DevicePoolId, device: Device, test: Test) {
        execute(poolId) { it.testFailed(test, device) }
        println("${toPercent(progress(poolId))} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} failed")
    }

    fun testPassed(poolId: DevicePoolId, device: Device, test: Test) {
        execute(poolId) { it.testPassed(test, device) }
        println("${toPercent(progress(poolId))} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} ended")
    }

    fun testIgnored(poolId: DevicePoolId, device: Device, test: Test) {
        execute(poolId) { it.testIgnored(test, device) }
    }

    fun aggregateResult(): Boolean {
        return reporters.isNotEmpty() && reporters.values.all {
            it.aggregateResult()
        }
    }

    fun totalTests(poolId: DevicePoolId, size: Int) {
        execute(poolId) { it.totalTests(size) }
    }

    fun removeTests(poolId: DevicePoolId, count: Int) {
        execute(poolId) { it.removeTests(count) }
    }

    fun addTests(poolId: DevicePoolId, count: Int) {
        execute(poolId) { it.addTests(count) }
    }

    fun progress(): Float {
        val size = reporters.size
        return reporters.values.sumByDouble { it.progress().toDouble() }.toFloat() / size
    }

    fun progress(poolId: DevicePoolId): Float {
        return execute(poolId) { it.progress() }
    }
}
