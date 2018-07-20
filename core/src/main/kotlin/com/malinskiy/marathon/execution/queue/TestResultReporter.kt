package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import mu.KotlinLogging

class TestResultReporter(private val poolId: DevicePoolId,
                         private val analytics: Analytics,
                         shard: TestShard) {
    private val tests: HashMap<String, StateMachine<TestState, TestEvent, TestAction>> = HashMap()

    private val logger = KotlinLogging.logger("TestResultReporter")

    private fun createState(count: Int) = StateMachine.create<TestState, TestEvent, TestAction> {
        initialState(TestState.Added(count))
        state<TestState.Added> {
            on<TestEvent.Passed> {
                transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
            }
            on<TestEvent.Failed> {
                when (count > 1) {
                    true -> transitionTo(TestState.Executed(it.device, it.testResult, this.count - 1))
                    false -> transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                }
            }
            on<TestEvent.Remove> {
                transitionTo(this.copy(count = this.count - it.diff))
            }
        }
        state<TestState.Executed> {
            on<TestEvent.Failed> {
                when (count > 1) {
                    true -> transitionTo(this.copy(device = it.device, testResult = it.testResult, count = this.count - 1))
                    false -> transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                }
            }
            on<TestEvent.Remove> {
                transitionTo(this.copy(count = this.count - it.diff))
            }
            on<TestEvent.Passed> {
                transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
            }
            on<TestEvent.Retry> {
                transitionTo(this.copy(count = this.count + 1))
            }
        }
        state<TestState.Failed> {
        }
        state<TestState.Passed> {
        }
        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid
            if (validTransition !is StateMachine.Transition.Valid) {
                logger.error { "from ${it.fromState} event ${it.event}" }
                return@onTransition
            }
            val sideEffect = validTransition.sideEffect
            when (sideEffect) {
                is TestAction.SaveReport -> {
                    saveReport(sideEffect.device, sideEffect.testResult)
                }
            }
        }
    }

    private fun saveReport(device: Device, testResult: TestResult) {
        analytics.trackTestResult(poolId, device, testResult)
    }

    init {
        val allTests = shard.tests + shard.flakyTests
        allTests.groupBy { it }.map {
            val count = it.value.size
            it.key.toTestName() to createState(count)
        }.also {
            tests.putAll(it)
        }
    }

    fun testFinished(device: Device, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Passed(device, testResult))
    }

    fun testFailed(device: Device, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Failed(device, testResult))
    }

    fun retryTest(test: Test) {
        tests[test.toTestName()]?.transition(TestEvent.Retry)
    }

    fun removeTest(test: Test, diff: Int) {
        tests[test.toTestName()]?.transition(TestEvent.Remove(diff))
    }
}