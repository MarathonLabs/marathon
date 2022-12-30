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
    suspend fun set(file: String, path: String, value: String): Boolean {
        return criticalExec("-c", "'Set $path $value'", file)?.successfulOrNull()?.successful ?: false
    }

    suspend fun add(file: String, path: String, type: String, value: String): Boolean {
        return criticalExec("-c", "'Add $path $type $value'", file)?.successfulOrNull()?.successful ?: false
    }

    protected suspend fun criticalExec(vararg args: String): CommandResult? {
        return commandExecutor.safeExecute(timeoutConfiguration.shell, "/usr/libexec/PlistBuddy", *args)
    }
}

