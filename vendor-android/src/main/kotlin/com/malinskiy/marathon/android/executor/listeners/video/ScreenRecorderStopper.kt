package com.malinskiy.marathon.android.executor.listeners.video

import com.android.ddmlib.IDevice
import com.android.ddmlib.NullOutputReceiver
import org.apache.commons.lang3.StringUtils.isBlank
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.slf4j.LoggerFactory

internal class ScreenRecorderStopper(private val deviceInterface: IDevice) {
    private val nullOutputReceiver = NullOutputReceiver()
    private var hasFailed: Boolean = false

    fun stopScreenRecord(hasFailed: Boolean) {
        this.hasFailed = hasFailed
        var hasKilledScreenRecord = true
        var tries = 0
        while (hasKilledScreenRecord && tries++ < VIDEO_KILL_ATTEMPTS) {
            hasKilledScreenRecord = attemptToGracefullyKillScreenRecord()
            pauseBetweenProcessKill()
        }
    }

    private fun attemptToGracefullyKillScreenRecord(): Boolean {
        val receiver = CollectingShellOutputReceiver()
        try {
            deviceInterface.executeShellCommand("ps |grep screenrecord", receiver)
            val pid = extractPidOfScreenrecordProcess(receiver)
            if (isNotBlank(pid)) {
                logger.trace("Killing PID $pid on ${deviceInterface.serialNumber}")
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

    private fun extractPidOfScreenrecordProcess(receiver: CollectingShellOutputReceiver): String? {
        val output = receiver.compile()
        if (isBlank(output)) {
            return null
        }
        val split = output.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val pid = split[1]
        logger.trace("Extracted PID $pid from output $output")
        return pid
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ScreenRecorderStopper::class.java)
        private const val VIDEO_KILL_ATTEMPTS = 5
        private const val PAUSE_BETWEEN_RECORDER_PROCESS_KILL = 300
    }
}
