package com.malinskiy.marathon.analytics.tracker

import com.malinskiy.marathon.createDeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.generateTestResult
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class NoOpTrackerSpek : Spek({
    describe("NoOpTracker test") {
        it("should not do anything") {
            val tracker = NoOpTracker()
            val devicePoolId = DevicePoolId("test")
            val deviceInfo = createDeviceInfo()
            val testResult = generateTestResult()
            tracker.trackDeviceConnected(devicePoolId, deviceInfo)
            tracker.trackTestFinished(devicePoolId, deviceInfo, testResult)
            tracker.trackRawTestRun(devicePoolId, deviceInfo, testResult)
        }
    }
})
