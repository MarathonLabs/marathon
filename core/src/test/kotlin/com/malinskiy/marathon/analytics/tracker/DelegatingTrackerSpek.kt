package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.createDeviceInfo
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.generateTestResult
import com.nhaarman.mockitokotlin2.same
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
            val device: DeviceInfo = createDeviceInfo()
            delegatingTracker.trackDeviceConnected(devicePoolId, device)
            verify(tracker1).trackDeviceConnected(same(devicePoolId), same(device))
            verify(tracker2).trackDeviceConnected(same(devicePoolId), same(device))
        }

        it("should execute trackTestFinished function on all trackers") {
            val tracker1 = mock<Tracker>()
            val tracker2 = mock<Tracker>()
            val delegatingTracker = DelegatingTracker(listOf(tracker1, tracker2))
            val devicePoolId = DevicePoolId("test")
            val deviceInfo = createDeviceInfo()
            val testResult = generateTestResult()
            delegatingTracker.trackTestFinished(devicePoolId, deviceInfo, testResult)
            verify(tracker1).trackTestFinished(same(devicePoolId), same(deviceInfo), same(testResult))
            verify(tracker2).trackTestFinished(same(devicePoolId), same(deviceInfo), same(testResult))
        }

        it("should execute trackRawTestRun function on all trackers") {
            val tracker1 = mock<Tracker>()
            val tracker2 = mock<Tracker>()
            val delegatingTracker = DelegatingTracker(listOf(tracker1, tracker2))
            val devicePoolId = DevicePoolId("test")
            val deviceInfo = createDeviceInfo()
            val testResult = generateTestResult()
            delegatingTracker.trackRawTestRun(devicePoolId, deviceInfo, testResult)
            verify(tracker1).trackRawTestRun(same(devicePoolId), same(deviceInfo), same(testResult))
            verify(tracker2).trackRawTestRun(same(devicePoolId), same(deviceInfo), same(testResult))
        }
    }
})
