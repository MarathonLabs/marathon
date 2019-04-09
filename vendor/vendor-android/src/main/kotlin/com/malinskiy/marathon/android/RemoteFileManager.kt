package com.malinskiy.marathon.android

import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.IDevice
import com.android.ddmlib.IDevice.MNT_EXTERNAL_STORAGE
import com.android.ddmlib.NullOutputReceiver
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.testrunner.TestIdentifier
import com.malinskiy.marathon.log.MarathonLogging

import java.io.IOException

class RemoteFileManager(private val device: IDevice) {

    private val logger = MarathonLogging.logger("RemoteFileManager")

    private val outputDir = device.getMountPoint(MNT_EXTERNAL_STORAGE)

    private val nullOutputReceiver = NullOutputReceiver()

    fun removeRemotePath(device: IDevice, remotePath: String) {
        executeCommand(device, "rm $remotePath", "Could not delete remote file(s): $remotePath")
    }

    fun pullFile(remoteFilePath: String, localFilePath: String) {
        device.pullFile(remoteFilePath, localFilePath)
    }

    fun createRemoteDirectory(device: IDevice) {
        executeCommand(device, "mkdir $outputDir", "Could not create remote directory: $outputDir")
    }

    fun removeRemoteDirectory(device: IDevice) {
        executeCommand(device, "rm -r $outputDir", "Could not delete remote directory: $outputDir")
    }

    private fun executeCommand(device: IDevice, command: String, errorMessage: String) {
        try {
            device.safeExecuteShellCommand(command, nullOutputReceiver)
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
        return "$outputDir/$filename"
    }

    private fun videoFileName(test: TestIdentifier): String = "${test.className}-${test.testName}.mp4"
}
