package com.malinskiy.marathon.android.executor.listeners.video

import com.android.ddmlib.IDevice
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.android.toTest
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType

internal class ScreenRecorderTestRunListener(private val fileManager: FileManager,
                                             private val pool: DevicePoolId,
                                             device: AndroidDevice) : NoOpTestRunListener() {
    private val device: Device = device
    private val deviceInterface: IDevice = device.ddmsDevice

    private var hasFailed: Boolean = false
    private var screenRecorderStopper: ScreenRecorderStopper? = null

    override fun testStarted(test: TestIdentifier) {
        hasFailed = false
        val localVideoFile = fileManager.createFile(FileType.VIDEO, pool, device, test.toTest())
        screenRecorderStopper = ScreenRecorderStopper(deviceInterface)
        val screenRecorder = ScreenRecorder(deviceInterface, localVideoFile, test, screenRecorderStopper!!)
        Thread(screenRecorder, "ScreenRecorder").start()
    }

    override fun testFailed(test: TestIdentifier, trace: String) {
        hasFailed = true
    }

    override fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        screenRecorderStopper!!.stopScreenRecord(hasFailed)
    }

    override fun testIgnored(test: TestIdentifier) {
        screenRecorderStopper!!.stopScreenRecord(hasFailed)
    }

    override fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        screenRecorderStopper!!.stopScreenRecord(hasFailed)
    }
}
