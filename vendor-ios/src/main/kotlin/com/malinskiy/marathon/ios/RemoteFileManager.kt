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
    fun remoteFile(device: Device, file: File = File("")): File {
        return remoteDirectory(device = device).resolve(file)
    }

    fun createRemoteDirectory(device: Device) {
        executeCommand(device,
                """mkdir -p "${remoteDirectory(device)}"""",
                "Could not create remote directory ${remoteDirectory(device)}")
    }

    fun removeRemoteDirectory(device: Device) {
        executeCommand(device,
                """rm -rf "${remoteDirectory(device)}"""",
                "Unable to remove directory ${remoteDirectory(device)}")
    }

    private fun executeCommand(device: Device, command: String, errorMessage: String) {
        val iosDevice = device as? IOSDevice
        if (iosDevice == null) {
            logger.error("Incorrect device type (serial = ${device.serialNumber})")
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
            if (output.stdout.isNotEmpty()) {
                logger.info(output.stdout)
            }
            if (output.stderr.isNotEmpty()) {
                logger.error(output.stderr)
            }
        }
    }

    fun remoteVideoForTest(test: Test): String {
        return remoteFileForTest(videoFileName(test))
    }

    private fun remoteFileForTest(filename: String): String {
        return "$OUTPUT_DIR/$filename"
    }

    private fun videoFileName(test: Test): String = "${test.pkg}-${test.clazz}-${test.method}.mp4"
}
