package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.queue.TestAction
import com.malinskiy.marathon.execution.queue.TestEvent
import com.malinskiy.marathon.execution.queue.TestState

open class NoOpTracker : Tracker {

    override fun terminate() {}

    override fun trackTestTransition(poolId: DevicePoolId, transition: StateMachine.Transition<TestState, TestEvent, TestAction>) {
        notifyTestFinished(transition, poolId)
        notifyRawTestRun(transition, poolId)
    }

    private fun notifyRawTestRun(transition: StateMachine.Transition<TestState, TestEvent, TestAction>, poolId: DevicePoolId) {
        val (testResult: TestResult?, device: DeviceInfo?) = extractEventAndDevice(transition)

        // Don't report tests that didn't finish the execution
        if (testResult == null || device == null || testResult.status == TestStatus.INCOMPLETE) return

        trackRawTestRun(poolId, device, testResult)
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
                    trackTestFinished(poolId, sideEffect.deviceInfo, sideEffect.testResult)
                }
            }
        }
    }

    open fun trackRawTestRun(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult) {
    }

    open fun trackTestFinished(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult) {
    }

    override fun trackDeviceConnected(poolId: DevicePoolId, device: DeviceInfo) {
    }
}
