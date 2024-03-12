package com.malinskiy.marathon.apple.bin.swvers

import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.apple.model.Arch
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import java.time.Duration

/**
 * create or operate on universal files
 */
class SwVers(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    suspend fun getVersion(): String {
        return commandExecutor.criticalExecute(timeoutConfiguration.shell, "sw_vers", "--productVersion").successfulOrNull()?.combinedStdout?.trim()
            ?: throw DeviceSetupException("failed to detect operating system version")
    }
}
