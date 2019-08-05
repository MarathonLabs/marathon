package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName

class TestResultReporter(private val poolId: DevicePoolId,
                         private val analytics: Analytics,
                         shard: TestShard,
                         private val configuration: Configuration) {

    private val tests: HashMap<String, StateMachine<TestState, TestEvent, TestAction>> = HashMap()

    private val logger = MarathonLogging.logger("TestResultReporter")

    private fun createState(initialCount: Int) = StateMachine.create<TestState, TestEvent, TestAction> {
        initialState(TestState.Added(initialCount))
        state<TestState.Added> {
//            on<TestEvent.Incomplete> {
//                    dontTransition(TestAction.SaveReport(it.device, it.testResult))
//            }
            on<TestEvent.Passed> {
                if (!configuration.strictMode || count <= 1) {
                    transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                } else {
                    transitionTo(TestState.Executed(it.device, it.testResult, count - 1))
                }
            }
            on<TestEvent.Failed> {
                if (configuration.strictMode || count <= 1) {
                    transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                } else {
                    transitionTo(TestState.Executed(it.device, it.testResult, count - 1))
                }
            }
            on<TestEvent.Remove> {
                transitionTo(this.copy(count = this.count - it.diff))
            }
        }
        state<TestState.Executed> {
//            on<TestEvent.Incomplete> {
//                dontTransition(TestAction.SaveReport(it.device, it.testResult))
//            }
            on<TestEvent.Failed> {
                if (configuration.strictMode || count <= 1) {
                    transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                } else {
                    transitionTo(TestState.Executed(it.device, it.testResult, count - 1))
                }
            }
            on<TestEvent.Remove> {
                transitionTo(this.copy(count = this.count - it.diff))
            }
            on<TestEvent.Passed> {
                if (!configuration.strictMode || count <= 1) {
                    transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                } else {
                    transitionTo(TestState.Executed(it.device, it.testResult, count - 1))
                }
            }
            on<TestEvent.Retry> {
                transitionTo(this.copy(count = this.count + 1))
            }
        }
        state<TestState.Failed> {
        }
        state<TestState.Passed> {
            on<TestEvent.Failed> {
                dontTransition()
            }
        }
        onTransition {
            if (it as? StateMachine.Transition.Valid !is StateMachine.Transition.Valid) {
                logger.error { "from ${it.fromState} event ${it.event}" }
            }
            trackTestTransition(poolId, it)
        }
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

    fun testPassed(device: DeviceInfo, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Passed(device, testResult))
    }

    fun testFailed(device: DeviceInfo, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Failed(device, testResult))
    }

    fun testIncomplete(device: DeviceInfo, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Incomplete(device, testResult))
    }

    fun retryTest(device: DeviceInfo, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Retry(device, testResult))
    }

    fun removeTest(test: Test, diff: Int) {
        tests[test.toTestName()]?.transition(TestEvent.Remove(diff))
    }

    private fun trackTestTransition(poolId: DevicePoolId, transition: StateMachine.Transition<TestState, TestEvent, TestAction>) {
        notifyTestFinished(transition, poolId)
        notifyRawTestRun(transition, poolId)
    }

    private fun notifyRawTestRun(transition: StateMachine.Transition<TestState, TestEvent, TestAction>, poolId: DevicePoolId) {
        val (testResult: TestResult?, device: DeviceInfo?) = extractEventAndDevice(transition)

        // Don't report tests that didn't finish the execution
        if (testResult == null || device == null || testResult.status == TestStatus.INCOMPLETE) return
        analytics.trackRawTestRun(poolId, device, testResult)
    }

    private fun extractEventAndDevice(transition: StateMachine.Transition<TestState, TestEvent, TestAction>): Pair<TestResult?, DeviceInfo?> {
        val event = transition.event
        val testResult: TestResult? = when (event) {
            is TestEvent.Passed -> event.testResult
            is TestEvent.Incomplete -> event.testResult
            is TestEvent.Failed -> event.testResult
            is TestEvent.Retry -> event.testResult
            else -> null
        }
        val device: DeviceInfo? = when (event) {
            is TestEvent.Passed -> event.device
            is TestEvent.Incomplete -> event.device
            is TestEvent.Failed -> event.device
            is TestEvent.Retry -> event.device
            else -> null
        }
        return Pair(testResult, device)
    }

    private fun notifyTestFinished(transition: StateMachine.Transition<TestState, TestEvent, TestAction>, poolId: DevicePoolId) {
        val validTransition = transition as? StateMachine.Transition.Valid
        if (validTransition is StateMachine.Transition.Valid) {
            val sideEffect = validTransition.sideEffect
            when (sideEffect) {
                is TestAction.SaveReport -> {
                    analytics.trackTestFinished(poolId, sideEffect.deviceInfo, sideEffect.testResult)
                }
            }
        }
    }
}
