package com.malinskiy.marathon.ios

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.ios.cmd.remote.CommandExecutor
import com.malinskiy.marathon.ios.cmd.remote.CommandResult
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import java.io.File

object RemoteFileManager {

    private val logger = MarathonLogging.logger(javaClass.simpleName)

    private const val OUTPUT_DIR = "/tmp/marathon"

    fun remoteDirectory(device: Device): File {
        return File("$OUTPUT_DIR/${device.hashCode()}")
    }
    fun resolveInRemoteDirectory(device: Device, file: File = File("")): File {
        return remoteDirectory(device = device).resolve(file)
    }

    fun createRemoteDirectory(device: Device) {
        executeCommand(device,
                """mkdir -p "$OUTPUT_DIR"""",
                "Could not create remote directory $OUTPUT_DIR")
    }

    fun removeRemoteDirectory(device: Device) {
        executeCommand(device,
                """rm -rf "$OUTPUT_DIR"""",
                "Unable to remove directory $OUTPUT_DIR")
    }

    fun remoteVideoForTest(test: Test): String {
        return remoteFileForTest(videoFileName(test))
    }

    private fun executeCommand(device: Device, command: String, errorMessage: String) {
        val iosDevice = device as? IOSDevice
        if (iosDevice == null) {
            logger.error("Device  ${device.serialNumber}")
            return
        }

        var output: CommandResult? = null
        try {
            output = iosDevice.hostCommandExecutor.exec(command)
        } catch (e: Exception) {
            logger.error(errorMessage, e)
        }

        if (output == null || output.exitStatus != 0 ) {
            logger.error(errorMessage)
        }

        if (output != null) {
            logger.info(output.stdout)
            logger.error(output.stderr)
        }
    }

    private fun remoteFileForTest(filename: String): String {
        return "$OUTPUT_DIR/$filename"
    }

    private fun videoFileName(test: Test): String = "${test.pkg}-${test.clazz}-${test.method}.mp4"
}
