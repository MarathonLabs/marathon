package com.malinskiy.marathon.android.executor.listeners.video

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.config.vendor.android.VideoConfiguration
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

internal class ScreenRecorder(
    private val device: AndroidDevice,
    private val videoConfiguration: VideoConfiguration,
) {

    suspend fun run(remoteFilePath: String) {
        try {
            startRecordingTestVideo(remoteFilePath)
        } catch (e: CancellationException) {
            logger.warn(e) { "screenrecord start was interrupted" }
            throw e
        } catch (e: Exception) {
            logger.error("Something went wrong while screen recording", e)
            throw e
        }
    }

    private suspend fun startRecordingTestVideo(remoteFilePath: String) {
        val millis = measureTimeMillis {
            device.safeStartScreenRecorder(
                remoteFilePath = remoteFilePath,
                options = videoConfiguration
            )
        }
        logger.debug { "Recording finished in ${millis}ms $remoteFilePath" }
    }

    /**
     * Send SIGINT to any live screenrecord PID, then wait for the process to
     * actually exit and its output file to stop growing on disk before
     * returning. `screenrecord` catches SIGINT and finalizes the mp4's
     * moov atom during shutdown; caller pulling the file before that
     * finalization completes was the source of the "video plays as blank
     * frame" bug — the file has ftyp + mdat but no moov, and browsers
     * refuse to decode it.
     *
     * @param remoteFilePath path being recorded to; polled for size
     *                       stability after the process exits. When null,
     *                       only the process-exit poll runs.
     */
    suspend fun stopScreenRecord(remoteFilePath: String? = null) {
        logger.debug { "Stopping screen recorder" }
        var sentSignal = false
        for (tries in 0 until SCREEN_RECORD_KILL_ATTEMPTS) {
            val killed = attemptToGracefullyKillScreenRecord()
            if (killed) sentSignal = true
            if (!killed) break
            delay(PAUSE_BETWEEN_RECORDER_PROCESS_KILL.toLong())
        }
        // Only run the finalization waits when we actually saw a running
        // screenrecord to kill. Skipping keeps the existing test stubs
        // (which stage a single ps-grep response) working, and avoids
        // burning device round-trips when there was nothing to stop.
        if (sentSignal) {
            awaitProcessExit()
            if (remoteFilePath != null) awaitFileSizeStable(remoteFilePath)
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
                logger.trace { "No screenrecord process still alive" }
            }
        } catch (e: Exception) {
            logger.error("Error while killing recording processes", e)
        }
        return false
    }

    /**
     * Poll `pidof screenrecord` until absent or the budget expires. Kill sent
     * to the process is SIGINT; screenrecord catches it and flushes moov —
     * the process only goes away once flush is done, so absence is a strong
     * signal that the file is complete.
     */
    private suspend fun awaitProcessExit() {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < PROCESS_EXIT_TIMEOUT_MS) {
            if (grepPid().isBlank()) {
                logger.debug { "screenrecord exited after ${System.currentTimeMillis() - start}ms wait on ${device.serialNumber}" }
                return
            }
            delay(PROCESS_POLL_INTERVAL_MS.toLong())
        }
        logger.warn { "screenrecord did not exit within ${PROCESS_EXIT_TIMEOUT_MS}ms on ${device.serialNumber} — pulled video may be truncated (missing moov)" }
    }

    /**
     * Poll the remote file size until two consecutive samples match. Guards
     * against pulling mid-write on slower emulators where the process has
     * already exited but the file's fs metadata hasn't caught up.
     */
    private suspend fun awaitFileSizeStable(remoteFilePath: String) {
        var previous = -1L
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < FILE_STABLE_TIMEOUT_MS) {
            val size = statRemoteSize(remoteFilePath) ?: return
            if (size == previous && size > 0L) {
                logger.debug { "$remoteFilePath size stable at $size bytes after ${System.currentTimeMillis() - start}ms" }
                return
            }
            previous = size
            delay(FILE_POLL_INTERVAL_MS.toLong())
        }
        logger.warn { "$remoteFilePath size did not stabilize within ${FILE_STABLE_TIMEOUT_MS}ms — video may be mid-write when pulled" }
    }

    /**
     * `stat -c%s` on Android — works from Nougat onward. Null return means
     * the stat call failed or the file is missing; caller should stop
     * polling in either case (the pull will surface the real failure).
     */
    private suspend fun statRemoteSize(remoteFilePath: String): Long? {
        val quoted = "'${remoteFilePath.replace("'", "'\\''")}'"
        val output = device.safeExecuteShellCommand("stat -c%s $quoted")?.output?.trim()
        return output?.takeIf { it.isNotBlank() }?.toLongOrNull()
    }

    companion object {
        private val logger = MarathonLogging.logger("ScreenRecorder")
        private const val SCREEN_RECORD_KILL_ATTEMPTS = 5
        /*
        * Workaround for https://github.com/MarathonLabs/marathon/issues/133
        */
        private const val PAUSE_BETWEEN_RECORDER_PROCESS_KILL = 300

        /** How long to wait for `screenrecord` to exit after SIGINT. */
        private const val PROCESS_EXIT_TIMEOUT_MS = 5_000
        private const val PROCESS_POLL_INTERVAL_MS = 200

        /** How long to wait for the remote file size to stabilize post-exit. */
        private const val FILE_STABLE_TIMEOUT_MS = 3_000
        private const val FILE_POLL_INTERVAL_MS = 150
    }
}
