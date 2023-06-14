package com.malinskiy.marathon.ios.bin.lipo

import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.model.Arch
import java.time.Duration

/**
 * create or operate on universal files
 */
class Lipo(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    suspend fun getArch(path: String): List<Arch> {
        val stdout = criticalExec(timeoutConfiguration.shell, path, "-archs").successfulOrNull()?.combinedStdout
            ?: throw DeviceSetupException("failed to detect binary format for $path")
        return stdout.split(" ").map { it.trim() }.filter { it.isNotBlank() }.map { rawValue ->
            Arch.values().firstOrNull {
                it.value == rawValue
            } ?: throw DeviceSetupException("Unknown arch=$rawValue, supported values are [${Arch.values().joinToString { it.value }}]")
        }
    }

    suspend fun removeArch(path: String, arch: Arch) {
        criticalExec(timeoutConfiguration.shell, path, "-remove", arch.value, "-output", path).successfulOrNull()?.combinedStdout
    }

    protected suspend fun criticalExec(
        timeout: Duration,
        vararg args: String,
    ): CommandResult {
        return commandExecutor.criticalExecute(timeout, "lipo", *args)
    }
}

