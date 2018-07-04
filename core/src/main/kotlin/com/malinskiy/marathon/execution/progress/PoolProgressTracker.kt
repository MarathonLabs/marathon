package com.malinskiy.marathon.execution.progress

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.Test
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat

class PoolProgressTracker {

    private sealed class Status {
        object Started : Status()
        object Ended : Status()
        object Failed : Status()
        object Ignored : Status()
    }

    private val tests = mutableMapOf<Test, Status>()

    private fun updateStatus(test: Test, newStatus: Status) {
        val prev = tests[test]
        tests[test] = when (prev) {
            is Status.Started -> {
                when (newStatus) {
                    is Status.Ended, Status.Failed, Status.Ignored -> {
                        newStatus
                    }
                    else -> {
                        throw IllegalArgumentException("old state = $prev and new state $newStatus")
                    }
                }
            }
            is Status.Ended -> {
                when (newStatus) {
                    is Status.Started -> {
                        newStatus
                    }
                    else -> {
                        throw IllegalArgumentException("old state = $prev and new state $newStatus")
                    }
                }
            }
            is Status.Failed -> {
                when (newStatus) {
                    is Status.Started -> {
                        newStatus
                    }
                    is Status.Ended -> {
                        prev
                    }
                    else -> {
                        throw IllegalArgumentException("old state = $prev and new state $newStatus")
                    }
                }
            }
            is Status.Ignored -> {
                when (newStatus) {
                    is Status.Started -> {
                        newStatus
                    }
                    else -> {
                        throw IllegalArgumentException("old state = $prev and new state $newStatus")
                    }
                }
            }
            null -> newStatus
        }
    }

    private val TEST_TIME = SimpleDateFormat("mm.ss")

    var totalTests = 0
    var completed = 0

    fun testStarted(test: Test, device: Device) {
        updateStatus(test, Status.Started)
    }

    fun testFailed(test: Test, device: Device) {
        updateStatus(test, Status.Failed)
    }

    fun testEnded(test: Test, device: Device) {
        completed++
        updateStatus(test, Status.Ended)
    }

    fun testIgnored(test: Test, device: Device) {
        updateStatus(test, Status.Ignored)
    }

    fun aggregateResult(): Boolean = tests.all {
        it.value != Status.Failed
    }

    fun totalTests(size: Int) {
        totalTests = size
    }

    fun removeTests(count: Int) {
        totalTests -= count
    }

    fun progress(): Float {
        return completed.toFloat() / totalTests.toFloat()
    }
}