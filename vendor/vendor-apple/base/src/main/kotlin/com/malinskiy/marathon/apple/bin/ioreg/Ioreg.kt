package com.malinskiy.marathon.apple.bin.ioreg

import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException

/**
 * show I/O Kit registry
 */
class Ioreg(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    suspend fun getUDID() = getParam("IOPlatformUUID")

    suspend fun getManufacturer() = getParam("manufacturer")

    suspend fun getModel() = getParam("model")

    private suspend fun getParam(name: String): String {
        return commandExecutor.criticalExecute(
            timeoutConfiguration.shell,
            "sh", "-c",
            "'ioreg -ad2 -c IOPlatformExpertDevice | plutil -extract IORegistryEntryChildren.0.$name raw -'",
        ).successfulOrNull()?.combinedStdout?.trim() ?: throw DeviceSetupException("failed to detect UDID")
    }
}

