package com.malinskiy.marathon.android.executor.listeners.video

import com.android.ddmlib.IDevice
import com.android.ddmlib.NullOutputReceiver
import mu.KotlinLogging

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

    private fun attemptToGracefullyKillScreenRecord(): Boolean {
        val receiver = CollectingShellOutputReceiver()
        try {
            deviceInterface.executeShellCommand("ps |grep screenrecord", receiver)
            val pid = extractPidOfScreenRecordProcess(receiver)
            if (pid.isNotBlank()) {
                logger.trace("Killing PID {} on {}", pid, deviceInterface.serialNumber)
                deviceInterface.executeShellCommand("kill -2 $pid", nullOutputReceiver)
                return true
            }
            logger.trace("Did not kill any screen recording process")
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
        private val logger = KotlinLogging.logger("ScreenRecorderStopper")
        private const val SCREEN_RECORD_KILL_ATTEMPTS = 100
        private const val PAUSE_BETWEEN_RECORDER_PROCESS_KILL = 50
    }

}
