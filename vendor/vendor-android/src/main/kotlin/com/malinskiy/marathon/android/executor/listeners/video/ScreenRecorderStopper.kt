package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.log.MarathonLogging

internal class ScreenRecorderStopper(private val device: AndroidDevice) {
    suspend fun stopScreenRecord() {
        var hasKilledScreenRecord = true
        var tries = 0
        while (hasKilledScreenRecord && tries++ < SCREEN_RECORD_KILL_ATTEMPTS) {
            hasKilledScreenRecord = attemptToGracefullyKillScreenRecord()
            pauseBetweenProcessKill()
        }
    }

    private suspend fun grepPid(): String {
        val output = if (device.version.isGreaterOrEqualThan(26)) {
            device.safeExecuteShellCommand("ps -A | grep screenrecord")?.output ?: ""
        } else {
            device.safeExecuteShellCommand("ps | grep screenrecord")?.output ?: ""
        }

        if (output.isBlank()) {
            return ""
        }

        val lastLine = output.lines().last { it.isNotEmpty() }
        val split = lastLine.split(' ').filter { it.isNotBlank() }
        val pid = split.getOrNull(1)?.let { it.toIntOrNull()?.toString() } ?: ""
        logger.trace("Extracted PID {} from output {}", pid, output)
        return pid
    }

    private suspend fun attemptToGracefullyKillScreenRecord(): Boolean {
        try {
            val pid = grepPid()
            if (pid.isNotBlank()) {
                logger.trace("Killing PID {} on {}", pid, device.serialNumber)
                device.safeExecuteShellCommand("kill -2 $pid")
                return true
            } else {
                logger.warn { "Did not kill any screen recording process" }
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

    companion object {
        private val logger = MarathonLogging.logger("ScreenRecorderStopper")
        private const val SCREEN_RECORD_KILL_ATTEMPTS = 5
        /*
        * Workaround for https://github.com/MarathonLabs/marathon/issues/133
        */
        private const val PAUSE_BETWEEN_RECORDER_PROCESS_KILL = 300
    }

}
