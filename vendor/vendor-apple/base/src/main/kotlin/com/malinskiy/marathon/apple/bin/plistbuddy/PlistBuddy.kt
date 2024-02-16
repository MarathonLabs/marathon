package com.malinskiy.marathon.apple.bin.plistbuddy

import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.config.vendor.apple.ios.TimeoutConfiguration

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

