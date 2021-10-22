package com.malinskiy.marathon.execution.progress.tracker

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.test.Test
import java.util.concurrent.atomic.AtomicInteger

class PoolProgressTracker(private val configuration: Configuration) {

    private val tests = mutableMapOf<Test, StateMachine<ProgressTestState, ProgressEvent, Any>>()
    private val runtimeDiscoveredTests = mutableSetOf<Test>()

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
                if (configuration.strictMode) {
                    transitionTo(ProgressTestState.Failed)
                } else {
                    dontTransition()
                }
            }
            on<ProgressEvent.Ignored> {
                dontTransition()
            }
        }
        state<ProgressTestState.Failed> {
            on<ProgressEvent.Passed> {
                if (configuration.strictMode) {
                    dontTransition()
                } else {
                    transitionTo(ProgressTestState.Passed)
                }
            }

        }
        state<ProgressTestState.Ignored> {
            on<ProgressEvent.Passed> {
                transitionTo(ProgressTestState.Passed)
            }
        }
    }

    private fun updateStatus(test: Test, newStatus: ProgressEvent) {
        synchronized(tests) {
            tests[test]?.transition(newStatus)
        }
    }

    private val expectedTestCount = AtomicInteger(0)
    private val completed = AtomicInteger(0)
    private val failed = AtomicInteger(0)
    private val ignored = AtomicInteger(0)
    private val retries = AtomicInteger(0)

    fun testStarted(test: Test) {
        synchronized(tests) {
            tests.computeIfAbsent(test) { _ -> createState() }
        }
    }

    fun testFailed(test: Test) {
        failed.updateAndGet {
            it + 1
        }
        updateStatus(test, ProgressEvent.Failed)
    }

    fun testPassed(test: Test) {
        completed.updateAndGet {
            it + 1
        }
        updateStatus(test, ProgressEvent.Passed)
    }

    fun testIgnored(test: Test) {
        ignored.updateAndGet {
            it + 1
        }
        updateStatus(test, ProgressEvent.Ignored)
    }

    fun aggregateResult(): Boolean {
        synchronized(tests) {
            return if (tests.size == expectedTestCount.get()) {
                tests.all {
                    when (it.value.state) {
                        is ProgressTestState.Passed -> true
                        is ProgressTestState.Ignored -> true
                        else -> false
                    }
                }
            } else {
                false
            }
        }
    }

    fun testCountExpectation(size: Int) {
        expectedTestCount.set(size)
    }

    fun removeTests(count: Int) {
        expectedTestCount.updateAndGet {
            it - count
        }
    }

    fun progress(): Float {
        return (completed.toFloat() + failed.toFloat() + ignored.toFloat()) / (expectedTestCount.toFloat() + retries.toFloat())
    }

    /**
     * This is for parameterized test discovery that can happen at runtime
     * Unfortunately a runtime discovered tests retries it will go through discovery process again, so we have to collect these
     */
    fun addTestDiscoveredDuringRuntime(test: Test) {
        synchronized(runtimeDiscoveredTests) {
            val before = runtimeDiscoveredTests.size
            runtimeDiscoveredTests.add(test)
            val after = runtimeDiscoveredTests.size
            expectedTestCount.updateAndGet {
                it + (after - before)
            }
        }
    }

    fun addTestRetries(count: Int) {
        retries.updateAndGet {
            it + count
        }
    }
}
