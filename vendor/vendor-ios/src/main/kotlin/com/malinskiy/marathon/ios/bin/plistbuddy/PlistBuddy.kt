package com.malinskiy.marathon.ios.bin.plistbuddy

import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.model.Arch
import java.time.Duration

/**
 * read and write values to plists
 */
class PlistBuddy(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    suspend fun set(file: String, path: String, value: String): List<String> {
        return criticalExec("-c", "'Set $path $value'", file).stdout
    }

    protected suspend fun criticalExec(vararg args: String): CommandResult {
        return commandExecutor.criticalExecute(timeoutConfiguration.shell, "/usr/libexec/PlistBuddy", *args)
    }
}

