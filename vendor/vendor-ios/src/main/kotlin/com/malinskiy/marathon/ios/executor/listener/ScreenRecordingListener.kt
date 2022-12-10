package com.malinskiy.marathon.ios.executor.listener

import com.malinskiy.marathon.ios.AppleDevice
import com.malinskiy.marathon.ios.RemoteFileManager
import com.malinskiy.marathon.ios.cmd.remote.CommandSession
import com.malinskiy.marathon.test.Test

class ScreenRecordingListener(
    private val testBatchId: String,
    private val device: AppleDevice,
) : AppleTestRunListener {
    private var session: CommandSession? = null

    override suspend fun testStarted(test: Test) {
        val remoteFile = RemoteFileManager.remoteVideoForTest(test, testBatchId)
        session = device.startVideoRecording(remoteFile)
    }

    override suspend fun testFailed(test: Test, startTime: Long, endTime: Long) {
        session?.let {
            stop(it)
        }
    }

    override suspend fun testPassed(test: Test, startTime: Long, endTime: Long) {
        session?.let {
            stop(it)
        }
    }

    override suspend fun afterTestRun() {
        session?.let {
            stop(it)
        }
    }
    
    private fun stop(commandSession: CommandSession) {
        commandSession.outputStream.write("\u0003".toByteArray())
        commandSession.outputStream.flush()
        commandSession.close()
    }
}
