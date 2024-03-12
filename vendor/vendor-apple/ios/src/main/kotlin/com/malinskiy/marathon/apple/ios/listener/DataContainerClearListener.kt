package com.malinskiy.marathon.apple.ios.listener

import com.malinskiy.marathon.apple.AppleTestBundleIdentifier
import com.malinskiy.marathon.apple.ios.AppleSimulatorDevice
import com.malinskiy.marathon.apple.listener.AppleTestRunListener
import com.malinskiy.marathon.test.TestBatch

class DataContainerClearListener(
    private val device: AppleSimulatorDevice,
    private val enabled: Boolean,
    private val testBatch: TestBatch,
    private val bundleIdentifier: AppleTestBundleIdentifier,
) : AppleTestRunListener {
    override suspend fun beforeTestRun() {
        super.beforeTestRun()
        if (enabled) {
            val bundleIds = testBatch.tests.map {
                bundleIdentifier.identify(it).appId
            }.toSet()
            bundleIds.forEach {
                device.clearAppContainer(it)
            }
        }
    }
}
