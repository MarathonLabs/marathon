package com.malinskiy.marathon.apple.bin.xcrun.simctl.service

import com.malinskiy.marathon.apple.bin.xcrun.simctl.SimctlService
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration

class MediaService(
    commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) : SimctlService(commandExecutor) {

    suspend fun addMedia(udid: String, remotePath: String): CommandResult {
        return criticalExec(
            timeout = timeoutConfiguration.importMedia,
            "addmedia", udid, remotePath
        )
    }
}
