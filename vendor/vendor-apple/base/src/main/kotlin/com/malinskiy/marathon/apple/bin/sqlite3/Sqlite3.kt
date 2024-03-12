package com.malinskiy.marathon.apple.bin.sqlite3

import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.apple.extensions.bashEscape
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import java.time.Duration

/**
 * Manipulate sqlite dbs
 *
 *
 */
class Sqlite3(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    /**
     * query
     */
    suspend fun query(path: String, query: String, sudo: Boolean = true) {
        criticalExec(timeoutConfiguration.shell, path.bashEscape(), query.bashEscape()).successfulOrNull()
            ?: throw DeviceSetupException("failed to execute sqlite path=\"$path\" query=\"$query\"")
    }


    protected suspend fun criticalExec(
        timeout: Duration,
        vararg args: String,
    ): CommandResult {
        return commandExecutor.criticalExecute(timeout, "sqlite3", *args)
    }
}
