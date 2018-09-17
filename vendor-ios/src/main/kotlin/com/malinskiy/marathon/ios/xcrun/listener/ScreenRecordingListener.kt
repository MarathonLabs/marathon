package com.malinskiy.marathon.ios.xcrun.listener

import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.recording.ScreenRecorder
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test

class ScreenRecordingListener(private val fileManager: FileManager,
                              private val pool: DevicePoolId,
                              private val device: IOSDevice) : TestRunListener {

    private val logger = MarathonLogging.logger(ScreenRecordingListener::class.java.simpleName)

    private var screenRecorder: ScreenRecorder? = null

    override fun testStarted(test: Test) {
        if(screenRecorder != null) {
            logger.error { "Something wrong. Recorder didn't finish but called again for the same device" }
        }
        screenRecorder = ScreenRecorder(device, test)
        screenRecorder?.run()
    }

    override fun testFailed(test: Test, startTime: Long, endTime: Long) {
        screenRecorder?.interrupt()
        screenRecorder = null
    }

    override fun testPassed(test: Test, startTime: Long, endTime: Long) {
        screenRecorder?.interrupt()
        screenRecorder = null
    }

    override fun batchFinished() {
        if(screenRecorder != null) {
            logger.error { "Something wrong. Recorder didn't finish but test batch already finished" }
        }
    }
}
