package com.malinskiy.marathon.android

import com.malinskiy.marathon.test.Test

class RemoteFileManager(private val device: AndroidDevice) {
    private val outputDir by lazy { device.externalStorageMount }

    suspend fun removeRemotePath(remotePath: String) {
        device.executeShellCommand("rm $remotePath", "Could not delete remote file(s): $remotePath")
    }

    suspend fun createRemoteDirectory() {
        device.executeShellCommand("mkdir $outputDir", "Could not create remote directory: $outputDir")
    }

    suspend fun removeRemoteDirectory() {
        device.executeShellCommand("rm -r $outputDir", "Could not delete remote directory: $outputDir")
    }

    fun remoteVideoForTest(test: Test): String {
        return remoteFileForTest(videoFileName(test))
    }

    private fun remoteFileForTest(filename: String): String {
        return "$outputDir/$filename"
    }

    private fun videoFileName(test: Test): String = "${test.pkg}.${test.clazz}-${test.method}.mp4"
}
