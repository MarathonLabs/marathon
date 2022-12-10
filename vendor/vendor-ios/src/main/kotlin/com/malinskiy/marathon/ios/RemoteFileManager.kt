package com.malinskiy.marathon.ios

import com.malinskiy.marathon.config.vendor.ios.Type
import com.malinskiy.marathon.extension.escape
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toClassName
import java.io.File

object RemoteFileManager {

    private val logger = MarathonLogging.logger(javaClass.simpleName)

    private const val OUTPUT_DIR = "/tmp/marathon"

    fun remoteDirectory(device: AppleDevice): File = File(OUTPUT_DIR)

    suspend fun createRemoteDirectory(device: AppleDevice) {
        executeCommand(
            device,
            """mkdir -p "${remoteDirectory(device)}"""",
            "Could not create remote directory ${remoteDirectory(device)}"
        )
    }

    suspend fun removeRemoteDirectory(device: AppleDevice) {
        executeCommand(
            device,
            """rm -rf "${remoteDirectory(device)}"""",
            "Unable to remove directory ${remoteDirectory(device)}"
        )
    }

    suspend fun removeRemotePath(device: AppleDevice, path: String) {
        executeCommand(
            device,
            "rm -rf $path",
            "Unable to remove path $path"
        )
    }

    fun remoteXctestrunFile(device: AppleDevice): File = remoteFile(device, File(xctestrunFileName(device)))

    /**
     * Omitting xcresult extension results in a symlink 
     */
    fun remoteXcresultFile(device: AppleDevice, batch: TestBatch): File = remoteFile(device, File(xcresultFileName(device, batch)))
    
    private fun xctestrunFileName(device: AppleDevice): String = "${device.udid}.xctestrun"
    private fun xcresultFileName(device: AppleDevice, batch: TestBatch): String =
        "${device.udid}.${batch.id}.xcresult"

    private fun remoteFile(device: AppleDevice, file: File): File = remoteDirectory(device = device).resolve(file)

    private suspend fun executeCommand(device: AppleDevice, command: String, errorMessage: String): String? {
        var output: CommandResult? = null
        try {
            output = device.executeWorkerCommand(command)
        } catch (e: Exception) {
            logger.error(errorMessage, e)
        }

        if (output == null || output.exitCode != 0) {
            logger.error(errorMessage)
        }

        if (output != null) {
            if (output.stderr.isNotEmpty()) {
                logger.error(output.stderr)
            }
            if (output.stdout.isNotEmpty()) {
                logger.info(output.stdout)
                return output.stdout
            }
        }
        return null
    }

    fun remoteVideoForTest(test: Test, testBatchId: String): String {
        return remoteFileForTest(videoFileName(test, testBatchId))
    }

    fun remoteScreenshot(udid: String, type: Type): String {
        return remoteFileForTest(screenshotFileName(udid, type))
    }

    private fun remoteFileForTest(filename: String): String {
        return "$OUTPUT_DIR/$filename"
    }
    
    private fun screenshotFileName(udid: String, type: Type): String {
        return "$udid.${type.value}"
    }

    private fun videoFileName(test: Test, testBatchId: String): String {
        val testSuffix = "-$testBatchId.mp4"
        val testName = "${test.toClassName('-')}-${test.method}".escape()
        return "$testName$testSuffix"
    }
}
