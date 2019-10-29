package com.malinskiy.marathon.android.executor.listeners.video

import com.android.ddmlib.IDevice
import com.android.ddmlib.NullOutputReceiver
import com.malinskiy.marathon.android.safeExecuteShellCommand
import com.malinskiy.marathon.log.MarathonLogging

internal class ScreenRecorderStopper(private val deviceInterface: IDevice) {
    private val nullOutputReceiver = NullOutputReceiver()

    fun stopScreenRecord() {
        var hasKilledScreenRecord = true
        var tries = 0
        while (hasKilledScreenRecord && tries++ < SCREEN_RECORD_KILL_ATTEMPTS) {
            hasKilledScreenRecord = attemptToGracefullyKillScreenRecord()
            pauseBetweenProcessKill()
        }
    }

    private fun grepPid(receiver: CollectingShellOutputReceiver) {
        if (deviceInterface.version.isGreaterOrEqualThan(26)) {
            deviceInterface.safeExecuteShellCommand("ps -A | grep screenrecord", receiver)
        } else {
            deviceInterface.safeExecuteShellCommand("ps | grep screenrecord", receiver)
        }
    }

    private fun attemptToGracefullyKillScreenRecord(): Boolean {
        val receiver = CollectingShellOutputReceiver()
        try {
            grepPid(receiver)
            val pid = extractPidOfScreenRecordProcess(receiver)
            if (pid.isNotBlank()) {
                logger.trace("Killing PID {} on {}", pid, deviceInterface.serialNumber)
                deviceInterface.safeExecuteShellCommand("kill -2 $pid", nullOutputReceiver)
                return true
            } else {
                logger.trace("Did not kill any screen recording process")
            }
        } catch (e: Exception) {
            logger.error("Error while killing recording processes", e)
        }
        return false
    }

    private fun pauseBetweenProcessKill() {
        try {
            Thread.sleep(PAUSE_BETWEEN_RECORDER_PROCESS_KILL.toLong())
        } catch (ignored: InterruptedException) {
        }

    }

    private fun extractPidOfScreenRecordProcess(receiver: CollectingShellOutputReceiver): String {
        val output = receiver.output()
        if (output.isBlank()) {
            return ""
        }
        val split = output.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val pid = split[1]
        logger.trace("Extracted PID {} from output {}", pid, output)
        return pid
    }

    companion object {
        private val logger = MarathonLogging.logger("ScreenRecorderStopper")
        private const val SCREEN_RECORD_KILL_ATTEMPTS = 5
        /*
        * Workaround for https://github.com/Malinskiy/marathon/issues/133
        */
        private const val PAUSE_BETWEEN_RECORDER_PROCESS_KILL = 300
    }

}
