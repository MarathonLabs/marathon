package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.strategy.ResultStrategy
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName

class TestResultReporter(private val poolId: DevicePoolId,
                         private val analytics: Analytics,
                         private val resultStrategy: ResultStrategy,
                         shard: TestShard) {

    private val tests: Map<String, StateMachine<TestState, TestEvent, TestAction>> = (shard.tests + shard.flakyTests)
            .groupBy {
                it
            }.map {
                it.key.toTestName() to resultStrategy.createStateMachine(it.value.size, ::trackTestTransition)
            }.toMap()
    
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

    private fun trackTestTransition(transition: StateMachine.Transition<TestState, TestEvent, TestAction>) {
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
            is TestEvent.Failed -> event.testResult
            is TestEvent.Retry -> event.testResult
            else -> null
        }
        val device: DeviceInfo? = when (event) {
            is TestEvent.Passed -> event.device
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
