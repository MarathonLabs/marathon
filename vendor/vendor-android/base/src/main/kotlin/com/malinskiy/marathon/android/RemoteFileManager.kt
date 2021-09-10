package com.malinskiy.marathon.android

import com.malinskiy.marathon.extension.escape
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test

class RemoteFileManager(private val device: AndroidDevice) {
    private val log = MarathonLogging.logger {}
    private val outputDir by lazy { device.externalStorageMount }

    suspend fun removeRemotePath(remotePath: String, recursive: Boolean = false) {
        val errorMessage = "Could not delete remote file(s): $remotePath"
        device.criticalExecuteShellCommand("rm ${if (recursive) "-r " else ""}$remotePath", errorMessage)
    }

    suspend fun createRemoteDirectory(remoteDir: String = outputDir) {
        device.criticalExecuteShellCommand("mkdir $remoteDir", "Could not create remote directory: $remoteDir")
    }

    suspend fun removeRemoteDirectory() {
        device.criticalExecuteShellCommand("rm -r $outputDir", "Could not delete remote directory: $outputDir")
    }

    fun remoteVideoForTest(test: Test, testBatchId: String): String {
        return remoteFileForTest(videoFileName(test, testBatchId))
    }

    private fun remoteFileForTest(filename: String): String {
        return "$outputDir/$filename"
    }

    private fun videoFileName(test: Test, testBatchId: String): String {
        "${test.pkg}.${test.clazz}-${test.method}-$testBatchId.mp4"

        val testSuffix = "-$testBatchId.mp4"
        val rawTestName = "${test.pkg}.${test.clazz}-${test.method}".escape()
        val testName = rawTestName.take(MAX_FILENAME - testSuffix.length)
        val fileName = "$testName$testSuffix"
        if (rawTestName.length > testName.length) {
            log.error("Remote filename length cannot exceed $MAX_FILENAME characters and has been trimmed to $fileName and can create a conflict. This happened because the combination of test class name and test name is too long.")
        }
        return fileName
    }

    companion object {
        const val MAX_FILENAME = 255
    }
}
