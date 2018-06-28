package com.malinskiy.marathon.execution.progress

import com.malinskiy.marathon.test.Test
import java.lang.IllegalArgumentException

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
                    is Status.Ended-> {
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

    fun testStarted(test: Test) {
        updateStatus(test, Status.Started)
    }

    fun testFailed(test: Test) {
        updateStatus(test, Status.Failed)
    }

    fun testEnded(test: Test) {
        updateStatus(test, Status.Ended)
    }

    fun testIgnored(test: Test) {
        updateStatus(test, Status.Ignored)
    }

    fun aggregateResult(): Boolean = tests.all {
        it.value != Status.Failed
    }
}