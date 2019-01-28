package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.queue.TestAction
import com.malinskiy.marathon.execution.queue.TestEvent
import com.malinskiy.marathon.execution.queue.TestState

internal class DelegatingTracker(private val trackers: List<Tracker>) : Tracker {
    override fun trackTestTransition(poolId: DevicePoolId, transition: StateMachine.Transition<TestState, TestEvent, TestAction>) {
        trackers.forEach {
            it.trackTestTransition(poolId, transition)
        }
    }

    override fun terminate() {
        trackers.forEach {
            it.terminate()
        }
    }

    override fun trackDeviceConnected(poolId: DevicePoolId, device: DeviceInfo) {
        trackers.forEach {
            it.trackDeviceConnected(poolId, device)
        }
    }
}
