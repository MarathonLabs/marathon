package com.malinskiy.marathon.apple.bin.xcrun.simctl.service

import com.malinskiy.marathon.apple.bin.xcrun.simctl.SimctlService
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration

class ApplicationService (commandExecutor: CommandExecutor,
                          private val timeoutConfiguration: TimeoutConfiguration,
) : SimctlService(commandExecutor) {

    suspend fun install(udid: String, remotePath: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.install,
            "install", udid, remotePath
        )
    }

    suspend fun containerPath(udid: String, bundleId: String, containerType: ContainerType): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.shell,
            "get_app_container", udid, bundleId, containerType.value
        )
    }

    enum class ContainerType(val value: String) {
        APPLICATION("app"),
        DATA("data"),
        GROUPS("groups")
    }

    /**
     * Terminates a running application with the given bundle ID on this device
     */
    suspend fun terminateApplication(udid: String, bundleId: String): CommandResult? {
        return safeExecute(
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
