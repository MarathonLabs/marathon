package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.queue.TestAction
import com.malinskiy.marathon.execution.queue.TestEvent
import com.malinskiy.marathon.execution.queue.TestState
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import org.amshove.kluent.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class DelegatingTrackerSpek : Spek({
    describe("delegating tracker test") {
        it("should execute trackDeviceConnected function on all trackers") {
            val tracker1 = mock<Tracker>()
            val tracker2 = mock<Tracker>()
            val delegatingTracker = DelegatingTracker(listOf(tracker1, tracker2))
            val devicePoolId = DevicePoolId("test")
            val device: Device = mock()
            delegatingTracker.trackDeviceConnected(devicePoolId, device)
            verify(tracker1).trackDeviceConnected(eq(devicePoolId), eq(device))
            verify(tracker2).trackDeviceConnected(eq(devicePoolId), eq(device))
        }

        it("should execute trackTestTransition function on all trackers") {
            val tracker1 = mock<Tracker>()
            val tracker2 = mock<Tracker>()
            val delegatingTracker = DelegatingTracker(listOf(tracker1, tracker2))
            val devicePoolId = DevicePoolId("test")
            val transition: StateMachine.Transition<TestState, TestEvent, TestAction> = mock()
            delegatingTracker.trackTestTransition(devicePoolId, transition)
            verify(tracker1).trackTestTransition(eq(devicePoolId), eq(transition))
            verify(tracker2).trackTestTransition(eq(devicePoolId), eq(transition))
        }
    }
})
