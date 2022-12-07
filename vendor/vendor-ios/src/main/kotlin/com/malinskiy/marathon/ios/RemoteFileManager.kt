package com.malinskiy.marathon.ios

import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import java.io.File

object RemoteFileManager {

    private val logger = MarathonLogging.logger(javaClass.simpleName)

    private const val OUTPUT_DIR = "/tmp/marathon"

    fun remoteDirectory(device: IOSDevice): File = File(OUTPUT_DIR)

    fun createRemoteDirectory(device: IOSDevice) {
        executeCommand(
            device,
            """mkdir -p "${remoteDirectory(device)}"""",
            "Could not create remote directory ${remoteDirectory(device)}"
        )
    }

    fun removeRemoteDirectory(device: IOSDevice) {
        executeCommand(
            device,
            """rm -rf "${remoteDirectory(device)}"""",
            "Unable to remove directory ${remoteDirectory(device)}"
        )
    }
    
    fun removeRemotePath(device: IOSDevice, path: String) {
        executeCommand(
            device,
            "rm -rf $path",
            "Unable to remove path $path"
        )
    }

    fun remoteXctestrunFile(device: IOSDevice): File = remoteFile(device, File(xctestrunFileName(device)))

    /**
     * Omitting xcresult extension results in a symlink 
     */
    fun remoteXcresultFile(device: IOSDevice, batch: TestBatch): File = remoteFile(device, File(xcresultFileName(device, batch)))
    
    private fun xctestrunFileName(device: IOSDevice): String = "${device.udid}.xctestrun"
    private fun xcresultFileName(device: IOSDevice, batch: TestBatch): String =
        "${device.udid}.${batch.id}.xcresult"

    private fun remoteFile(device: IOSDevice, file: File): File = remoteDirectory(device = device).resolve(file)

    private fun executeCommand(device: IOSDevice, command: String, errorMessage: String): String? {
        var output: CommandResult? = null
        try {
            output = device.hostCommandExecutor.execBlocking(command)
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

    fun remoteVideoForTest(test: Test): String {
        return remoteFileForTest(videoFileName(test))
    }

    private fun remoteFileForTest(filename: String): String {
        return "$OUTPUT_DIR/$filename"
    }

    private fun videoFileName(test: Test): String = "${test.pkg}-${test.clazz}-${test.method}.mp4"
}
