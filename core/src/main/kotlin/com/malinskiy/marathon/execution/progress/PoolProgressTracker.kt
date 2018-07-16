package com.malinskiy.marathon.execution.progress

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.Test
import java.util.concurrent.atomic.AtomicInteger

class PoolProgressTracker {

    private sealed class Status {
        object Started : Status()
        object Ended : Status()
        object Failed : Status()
        object Ignored : Status()
    }

    private val tests = mutableMapOf<Test, Status>()

    @Suppress("ThrowsCount")
    private fun updateStatus(test: Test, newStatus: Status) {
        tests[test] = newStatus
    }

    private val totalTests = AtomicInteger(0)
    private val completed = AtomicInteger(0)
    private val failed = AtomicInteger(0)

    fun testStarted(test: Test, device: Device) {
        updateStatus(test, Status.Started)
    }

    fun testFailed(test: Test, device: Device) {
        updateStatus(test, Status.Failed)
        failed.updateAndGet {
            it + 1
        }
    }

    fun testEnded(test: Test, device: Device) {
        completed.updateAndGet {
            it + 1
        }
        updateStatus(test, Status.Ended)
    }

    fun testIgnored(test: Test, device: Device) {
        updateStatus(test, Status.Ignored)
    }

    fun aggregateResult(): Boolean = tests.all {
        it.value != Status.Failed
    }

    fun totalTests(size: Int) {
        totalTests.set(size)
    }

    fun removeTests(count: Int) {
        totalTests.updateAndGet {
            it - count
        }
    }

    fun progress(): Float {
        return (completed.toFloat() + failed.toFloat()) / totalTests.toFloat()
    }

    fun addTests(count: Int) {
        totalTests.updateAndGet {
            it + count
        }
    }
}
