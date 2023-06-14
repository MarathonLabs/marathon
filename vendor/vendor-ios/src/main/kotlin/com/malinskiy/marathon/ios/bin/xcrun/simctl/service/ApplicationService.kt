package com.malinskiy.marathon.ios.bin.xcrun.simctl.service

import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.bin.xcrun.simctl.SimctlService

class ApplicationService (commandExecutor: CommandExecutor,
                          private val timeoutConfiguration: TimeoutConfiguration,
) : SimctlService(commandExecutor) {

    suspend fun install(udid: String, remotePath: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.install,
            "install", udid, remotePath
        )
    }

    /**
     * Terminates a running application with the given bundle ID on this device
     */
    suspend fun terminateApplication(udid: String, bundleId: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.shell,
            "terminate", udid, bundleId
        )
    }

    /**
     * Uninstalls an app with the given bundle ID from this device
     */
    suspend fun uninstallApplication(udid: String, bundleId: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.uninstall,
            "uninstall", udid, bundleId
        )
    }
}
