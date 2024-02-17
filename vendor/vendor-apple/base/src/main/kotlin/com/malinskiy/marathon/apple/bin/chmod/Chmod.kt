package com.malinskiy.marathon.apple.bin.chmod

import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.apple.extensions.bashEscape
import com.malinskiy.marathon.apple.model.Arch
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import java.time.Duration

class Chmod(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    suspend fun makeExecutable(path: String): String {
        return commandExecutor.criticalExecute(timeoutConfiguration.shell, "chmod", "+x", path.bashEscape()).successfulOrNull()?.combinedStdout?.trim()
            ?: throw DeviceSetupException("failed to detect operating system version")
    }
}
