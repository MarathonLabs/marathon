package com.malinskiy.marathon.ios.recording

import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.RemoteFileManager
import com.malinskiy.marathon.test.Test

class ScreenRecorder(private val device: IOSDevice,
                     private val test: Test) : Thread() {

    override fun run() {
        val command = "xcrun simctl io ${device.udid} recordVideo ${RemoteFileManager.remoteVideoForTest(test)}"
        val session = device.hostCommandExecutor.startSession(command)
        session.connect()

        while(!interrupted()) {
            Thread.sleep(500)
        }

        session.outputStream.write("\u0003".toByteArray())
        session.outputStream.flush()

        session.close()
    }
}
