package com.malinskiy.marathon.apple.bin.xcrun.simctl.service

import com.malinskiy.marathon.apple.bin.xcrun.simctl.SimctlService
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.apple.cmd.CommandSession
import com.malinskiy.marathon.config.vendor.apple.ios.Permission
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration
import java.time.Duration

class SpawnService(commandExecutor: CommandExecutor,
                   private val timeoutConfiguration: TimeoutConfiguration,
) : SimctlService(commandExecutor) {
    /**
     * Spawn a process by executing a given executable on a device
     */
    suspend fun spawn(udid: String, args: Array<String>, timeout: Duration = timeoutConfiguration.shell): CommandResult {
        return criticalExec(
            timeout = timeout,
            "spawn", udid, *args
        )
    }
}
