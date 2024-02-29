package com.malinskiy.marathon.apple.bin.csrutil

import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import java.time.Duration

/**
 * Create and manipulate code signatures
 * 
 * 
 */
class Csrutil(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    /**
     * Detects System Integrity Protection status
     */
    suspend fun status(): Boolean {
        val stdout = criticalExec(timeoutConfiguration.shell, "status").successfulOrNull()?.stdout
            ?: throw DeviceSetupException("failed to detect SIP status")

        return !stdout.contains("disabled")
    }


    protected suspend fun criticalExec(
        timeout: Duration,
        vararg args: String,
    ): CommandResult {
        return commandExecutor.criticalExecute(timeout, "csrutil", *args)
    }
}
