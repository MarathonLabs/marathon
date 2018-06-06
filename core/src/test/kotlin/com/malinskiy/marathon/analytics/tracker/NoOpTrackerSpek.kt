package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.Test
import com.nhaarman.mockito_kotlin.mock
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class NoOpTrackerSpek : Spek({
    describe("NoOpTracker test") {
        it("should not do anything") {
            val tracker = NoOpTracker()
            val poolId = DevicePoolId("id")
            val device: Device = mock()
            val test = Test("pkg", "clazz", "method", emptyList())
            val deviceInfo = DeviceInfo(
                    OperatingSystem("23"),
                    "serial",
                    "model",
                    "manufacturer",
                    NetworkState.CONNECTED,
                    emptyList(),
                    true
            )
            val testResult = TestResult(test, deviceInfo, TestStatus.PASSED, 200, 400, null)
            tracker.trackDeviceConnected(poolId, device)
            tracker.trackTestResult(poolId, device, testResult)
        }
    }
})
