package com.malinskiy.marathon.apple.bin.systemprofiler

import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException

/**
 * create or operate on universal files
 */
class SystemProfiler(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    suspend fun getProvisioningUdid(): String {
        val stdout = commandExecutor.criticalExecute(
            timeoutConfiguration.shell, "sh", "-c",
            "'system_profiler SPHardwareDataType'",
        ).successfulOrNull()?.combinedStdout?.trim()
        return stdout?.lines()?.find { it.contains("Provisioning UDID") }?.split(":")?.getOrNull(1)?.trim() ?: ""
    }
}
