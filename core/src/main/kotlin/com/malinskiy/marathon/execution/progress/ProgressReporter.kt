package com.malinskiy.marathon.execution.progress

import com.malinskiy.marathon.config.BuildManagementConfiguration
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.progress.tracker.PoolProgressTracker
import com.malinskiy.marathon.integration.buildmanagement.Teamcity
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

const val HUNDRED_PERCENT_IN_FLOAT: Float = 100.0f

class ProgressReporter(private val configuration: Configuration) {
    private val reporters = ConcurrentHashMap<DevicePoolId, PoolProgressTracker>()
    private val buildManagement = when(configuration.buildManagementConfiguration) {
        BuildManagementConfiguration.NoBuildManagementConfiguration -> Teamcity
        BuildManagementConfiguration.TeamCityConfiguration -> null
    }

    private inline fun <T> execute(poolId: DevicePoolId, f: (PoolProgressTracker) -> T): T {
        val reporter = reporters[poolId] ?: PoolProgressTracker(configuration)
        val result = f(reporter)
        reporters[poolId] = reporter
        return result
    }

    private fun toPercent(float: Float): String {
        val percent = (float * HUNDRED_PERCENT_IN_FLOAT).roundToInt()
        val format = "%02d%%"
        return String.format(format, percent)
    }

    fun testStarted(poolId: DevicePoolId, device: DeviceInfo, test: Test) {
        execute(poolId) { it.testStarted(test) }
        val message = "${toPercent(progress(poolId))} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} started"
        println(message)
        buildManagement?.setBuildMessage(message)
    }

    fun testFailed(poolId: DevicePoolId, device: DeviceInfo, test: Test) {
        execute(poolId) { it.testFailed(test) }
        val message = "${toPercent(progress(poolId))} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} failed"
        println(message)
        buildManagement?.setBuildMessage(message)
    }

    fun testPassed(poolId: DevicePoolId, device: DeviceInfo, test: Test) {
        execute(poolId) { it.testPassed(test) }
        val message = "${toPercent(progress(poolId))} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} ended"
        println(message)
        buildManagement?.setBuildMessage(message)
    }

    fun testIgnored(poolId: DevicePoolId, device: DeviceInfo, test: Test) {
        execute(poolId) { it.testIgnored(test) }
        val message = "${toPercent(progress(poolId))} | [${poolId.name}]-[${device.serialNumber}] ${test.toTestName()} ignored"
        println(message)
        buildManagement?.setBuildMessage(message)
    }

    fun aggregateResult(): Boolean {
        return reporters.isNotEmpty() && reporters.values.all {
            it.aggregateResult()
        }
    }

    fun testCountExpectation(poolId: DevicePoolId, size: Int) {
        execute(poolId) { it.testCountExpectation(size) }
    }

    fun removeTests(poolId: DevicePoolId, count: Int) {
        execute(poolId) { it.removeTests(count) }
    }

    fun addTestDiscoveredDuringRuntime(poolId: DevicePoolId, test: Test) {
        execute(poolId) { it.addTestDiscoveredDuringRuntime(test) }
    }

    fun addRetries(poolId: DevicePoolId, count: Int) {
        execute(poolId) { it.addTestRetries(count) }
    }

    fun progress(): Float {
        val size = reporters.size
        return reporters.values.sumOf {
            it.progress().toDouble()
        }.toFloat() / size
    }

    fun progress(poolId: DevicePoolId): Float {
        return execute(poolId) { it.progress() }
    }
}
