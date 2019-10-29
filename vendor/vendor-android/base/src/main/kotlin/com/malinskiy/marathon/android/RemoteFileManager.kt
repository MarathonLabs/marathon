package com.malinskiy.marathon.android

import com.malinskiy.marathon.test.Test

class RemoteFileManager(private val device: AndroidDevice) {
    private val outputDir = device.getExternalStorageMount()

    fun removeRemotePath(remotePath: String) {
        device.executeCommand("rm $remotePath", "Could not delete remote file(s): $remotePath")
    }

    fun pullFile(remoteFilePath: String, localFilePath: String) {
        device.pullFile(remoteFilePath, localFilePath)
    }

    fun createRemoteDirectory() {
        device.executeCommand("mkdir $outputDir", "Could not create remote directory: $outputDir")
    }

    fun removeRemoteDirectory() {
        device.executeCommand("rm -r $outputDir", "Could not delete remote directory: $outputDir")
    }

    fun remoteVideoForTest(test: Test): String {
        return remoteFileForTest(videoFileName(test))
    }

    private fun remoteFileForTest(filename: String): String {
        return "$outputDir/$filename"
    }

    private fun videoFileName(test: Test): String = "${test.pkg}.${test.clazz}-${test.method}.mp4"
}
