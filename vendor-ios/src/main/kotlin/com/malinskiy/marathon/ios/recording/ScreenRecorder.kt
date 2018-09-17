package com.malinskiy.marathon.ios.recording

import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.RemoteFileManager
import com.malinskiy.marathon.test.Test

class ScreenRecorder(private val device: IOSDevice,
                     private val test: Test) : Thread() {

    override fun run() {
        val command = device.hostCommandExecutor.startSession().exec("io ${device.udid} recordVideo /tmp/${RemoteFileManager.remoteVideoForTest(test)}")
        while(!interrupted()) {
            Thread.sleep(500)
        }

        command.outputStream.write("\u0003".toByteArray())
        command.outputStream.flush()
    }
}
