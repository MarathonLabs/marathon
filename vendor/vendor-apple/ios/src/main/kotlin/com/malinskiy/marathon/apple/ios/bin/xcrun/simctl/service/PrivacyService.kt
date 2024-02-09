package com.malinskiy.marathon.apple.ios.bin.xcrun.simctl.service

import com.malinskiy.marathon.apple.ios.bin.xcrun.simctl.SimctlService
import com.malinskiy.marathon.apple.ios.cmd.CommandExecutor
import com.malinskiy.marathon.apple.ios.cmd.CommandResult
import com.malinskiy.marathon.config.vendor.ios.Permission
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration

class PrivacyService(commandExecutor: CommandExecutor,
                private val timeoutConfiguration: TimeoutConfiguration,
) : SimctlService(commandExecutor) {
    /**
     * Grants access to the given service to an application with the given bundle ID
     */
    suspend fun grant(udid: String, service: Permission, bundleId: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.shell,
            "privacy", udid, "grant", service.value, bundleId
        )
    }

    /**
     * Revokes access to the given service from an application with the given bundle ID
     */
    suspend fun revoke(udid: String, service: Permission, bundleId: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.shell,
            "privacy", udid, "revoke", service.value, bundleId
        )
    }

    /**
     * Resets access to the given service from an application with the given bundle ID
     * This will cause the OS to ask again when this app requests permission to use the given service
     */
    suspend fun reset(udid: String, service: Permission, bundleId: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.shell,
            "privacy", udid, "reset", service.value, bundleId
        )
    }

    /**
     * Resets access to the given service from all applications running on the device
     */
    suspend fun resetAll(udid: String, service: Permission): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.shell,
            "privacy", udid, "reset", service.value
        )
    }
}
