package com.malinskiy.marathon.execution.progress.tracker

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.test.Test
import java.util.concurrent.atomic.AtomicInteger

class PoolProgressTracker {

    private val tests = mutableMapOf<Test, StateMachine<ProgressTestState, ProgressEvent, Any>>()

    private fun createState() = StateMachine.create<ProgressTestState, ProgressEvent, Any> {
        initialState(ProgressTestState.Started)
        state<ProgressTestState.Started> {
            on<ProgressEvent.Failed> {
                transitionTo(ProgressTestState.Failed)
            }
            on<ProgressEvent.Passed> {
                transitionTo(ProgressTestState.Passed)
            }
            on<ProgressEvent.Ignored> {
                transitionTo(ProgressTestState.Ignored)
            }
        }
        state<ProgressTestState.Passed> {
            on<ProgressEvent.Failed> {
                dontTransition()
            }
            on<ProgressEvent.Ignored> {
                dontTransition()
            }
        }
        state<ProgressTestState.Failed> {
            on<ProgressEvent.Passed> {
                transitionTo(ProgressTestState.Passed)
            }
        }
        state<ProgressTestState.Ignored> {
            on<ProgressEvent.Passed> {
                transitionTo(ProgressTestState.Passed)
            }
        }
    }

    private fun updateStatus(test: Test, newStatus: ProgressEvent) = tests[test]?.transition(newStatus)

    private val totalTests = AtomicInteger(0)
    private val completed = AtomicInteger(0)
    private val failed = AtomicInteger(0)

    fun testStarted(test: Test) {
        tests.computeIfAbsent(test) { _ -> createState() }
    }

    fun testFailed(test: Test) {
        if(tests[test]?.state == ProgressTestState.Passed) {
            //Return early because the test already passed
            return
        }

        updateStatus(test, ProgressEvent.Failed)
        failed.updateAndGet {
            it + 1
        }
    }

    fun testPassed(test: Test) {
        completed.updateAndGet {
            it + 1
        }
        updateStatus(test, ProgressEvent.Passed)
    }

    fun testIgnored(test: Test) {
        updateStatus(test, ProgressEvent.Ignored)
    }

    fun aggregateResult(): Boolean = tests.all {
        when (it.value.state) {
            is ProgressTestState.Passed -> true
            is ProgressTestState.Ignored -> true
            else -> false
        }
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
