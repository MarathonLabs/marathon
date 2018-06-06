package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.analytics.tracker.DelegatingTracker
import com.malinskiy.marathon.analytics.tracker.Tracker
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
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

        it("should execute trackTestResult function on all trackers") {
            val tracker1 = mock<Tracker>()
            val tracker2 = mock<Tracker>()
            val delegatingTracker = DelegatingTracker(listOf(tracker1, tracker2))
            val devicePoolId = DevicePoolId("test")
            val device: Device = mock()
            val test = Test("pkg", "clazz", "method", emptyList())
            val testResult = TestResult(test, DeviceInfo(
                    OperatingSystem("23"),
                    "serial",
                    "model",
                    "manufacturer",
                    NetworkState.CONNECTED,
                    emptyList(),
                    true
            ), TestStatus.PASSED, 100, 200, null)
            delegatingTracker.trackTestResult(devicePoolId, device, testResult)
            verify(tracker1).trackTestResult(eq(devicePoolId), eq(device), eq(testResult))
            verify(tracker2).trackTestResult(eq(devicePoolId), eq(device), eq(testResult))
        }
    }
})
