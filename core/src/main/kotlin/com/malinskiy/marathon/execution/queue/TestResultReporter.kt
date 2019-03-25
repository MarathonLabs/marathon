package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName

class TestResultReporter(private val poolId: DevicePoolId,
                         private val analytics: Analytics,
                         private val strictMode: Boolean,
                         shard: TestShard) {

    private val tests: HashMap<String, StateMachine<TestState, TestEvent, TestAction>> = HashMap()

    private val logger = MarathonLogging.logger("TestResultReporter")

    private fun createState(initialCount: Int) = StateMachine.create<TestState, TestEvent, TestAction> {
        initialState(TestState.Added(initialCount))
        state<TestState.Added> {
            on<TestEvent.Passed> {
                val result = listOf(it.device to it.testResult)
                when (this.count > 1) {
                    true -> transitionTo(TestState.Executing(this.count - 1, result))
                    false -> transitionTo(TestState.Executed(result), createSaveReportAction(result))
                }
            }
            on<TestEvent.Failed> {
                val result = listOf(it.device to it.testResult)
                when (this.count > 1) {
                    true -> transitionTo(TestState.Executing(this.count - 1, result))
                    false -> transitionTo(TestState.Executed(result), createSaveReportAction(result))
                }
            }
            on<TestEvent.Remove> {
                transitionTo(this.copy(count = this.count - it.diff))
            }
        }
        state<TestState.Executing> {
            on<TestEvent.Failed> {
                val results = this.testRuns + (it.device to it.testResult)
                when (this.count > 1) {
                    true -> transitionTo(TestState.Executing(this.count - 1, results))
                    false -> transitionTo(TestState.Executed(results), createSaveReportAction(results))
                }
            }
            on<TestEvent.Passed> {
                val results = this.testRuns + (it.device to it.testResult)
                when (this.count > 1) {
                    true -> transitionTo(TestState.Executing(this.count - 1, results))
                    false -> transitionTo(TestState.Executed(results), createSaveReportAction(results))
                }
            }
            on<TestEvent.Remove> {
                transitionTo(this.copy(count = this.count - it.diff))
            }
            on<TestEvent.Retry> {
                transitionTo(this.copy(count = this.count + 1))
            }
        }
        state<TestState.Executed> {
        }
        onTransition {
            if (it as? StateMachine.Transition.Valid !is StateMachine.Transition.Valid) {
                logger.error { "from ${it.fromState} event ${it.event}" }
            }
            analytics.trackTestTransition(poolId, it)
        }
    }

    private fun createSaveReportAction(results: List<Pair<DeviceInfo, TestResult>>): TestAction.SaveReport {
        val (device, testResult) = results.find {
            (strictMode == !it.second.isSuccess) && !it.second.isIgnored
        } ?: results.first()

        return TestAction.SaveReport(device, testResult)
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

    fun testFinished(device: DeviceInfo, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Passed(device, testResult))
    }

    fun testFailed(device: DeviceInfo, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Failed(device, testResult))
    }

    fun retryTest(device: DeviceInfo, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Retry(device, testResult))
    }

    fun removeTest(test: Test, diff: Int) {
        tests[test.toTestName()]?.transition(TestEvent.Remove(diff))
    }
}
