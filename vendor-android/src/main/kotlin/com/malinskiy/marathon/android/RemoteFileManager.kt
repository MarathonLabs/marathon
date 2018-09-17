package com.malinskiy.marathon.android

import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.IDevice
import com.android.ddmlib.NullOutputReceiver
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.log.MarathonLogging

import java.io.IOException

object RemoteFileManager {

    private val logger = MarathonLogging.logger("RemoteFileManager")

    private const val OUTPUT_DIR = "/sdcard/marathon"
    private val NO_OP_RECEIVER = NullOutputReceiver()

    fun removeRemotePath(device: IDevice, remotePath: String) {
        executeCommand(device, "rm $remotePath", "Could not delete remote file(s): $remotePath")
    }

    fun createRemoteDirectory(device: IDevice) {
        executeCommand(device, "mkdir $OUTPUT_DIR", "Could not create remote directory: $OUTPUT_DIR")
    }

    fun removeRemoteDirectory(device: IDevice) {
        executeCommand(device, "rm -r $OUTPUT_DIR", "Could not delete remote directory: $OUTPUT_DIR")
    }

    private fun executeCommand(device: IDevice, command: String, errorMessage: String) {
        try {
            device.executeShellCommand(command, NO_OP_RECEIVER)
        } catch (e: TimeoutException) {
            logger.error(errorMessage, e)
        } catch (e: AdbCommandRejectedException) {
            logger.error(errorMessage, e)
        } catch (e: ShellCommandUnresponsiveException) {
            logger.error(errorMessage, e)
        } catch (e: IOException) {
            logger.error(errorMessage, e)
        }
    }

    fun remoteVideoForTest(test: TestIdentifier): String {
        return remoteFileForTest(videoFileName(test))
    }

    private fun remoteFileForTest(filename: String): String {
        return "$OUTPUT_DIR/$filename"
    }

    private fun videoFileName(test: TestIdentifier): String = "${test.className}-${test.testName}.mp4"
}
