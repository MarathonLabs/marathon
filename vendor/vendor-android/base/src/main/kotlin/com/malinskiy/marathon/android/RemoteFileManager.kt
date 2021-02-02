package com.malinskiy.marathon.android

import com.malinskiy.marathon.test.Test

class RemoteFileManager(private val device: AndroidDevice) {
    private val outputDir by lazy { device.externalStorageMount }

    suspend fun removeRemotePath(remotePath: String, recursive: Boolean = false) {
        val errorMessage = "Could not delete remote file(s): $remotePath"
        device.criticalExecuteShellCommand("rm ${if (recursive) "-r" else ""} $remotePath", errorMessage)
    }

    suspend fun createRemoteDirectory(remoteDir: String = outputDir) {
        device.criticalExecuteShellCommand("mkdir $remoteDir", "Could not create remote directory: $remoteDir")
    }

    suspend fun removeRemoteDirectory() {
        device.criticalExecuteShellCommand("rm -r $outputDir", "Could not delete remote directory: $outputDir")
    }

    fun remoteVideoForTest(test: Test): String {
        return remoteFileForTest(videoFileName(test))
    }

    private fun remoteFileForTest(filename: String): String {
        return "$outputDir/$filename"
    }

    private fun videoFileName(test: Test): String = "${test.pkg}.${test.clazz}-${test.method}.mp4"
}
