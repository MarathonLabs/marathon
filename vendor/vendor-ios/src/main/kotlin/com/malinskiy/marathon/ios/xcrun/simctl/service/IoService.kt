package com.malinskiy.marathon.ios.xcrun.simctl.service

import com.malinskiy.marathon.config.vendor.ios.Codec
import com.malinskiy.marathon.config.vendor.ios.Display
import com.malinskiy.marathon.config.vendor.ios.Mask
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.config.vendor.ios.Type
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.xcrun.simctl.SimctlService

class IoService(
    commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) : SimctlService(commandExecutor) {
    suspend fun screenshot(udid: String, destination: String, type: Type, display: Display, mask: Mask): Boolean {
        return safeExecute(
            timeout = timeoutConfiguration.screenshot,
            "io", udid, "screenshot", "--type=${type.value}", "--display=${display.value}", "--mask=${mask.value}", destination
        )
            ?.successful ?: false
    }

    /**
     * recording video requires us to send SIGINT which is not available via JVM Process API
     * SIGINT also doesn't work using ssh because OpenSSH terminates the connection when the exec request's process receives SIGINT
     */
    suspend fun recordVideo(udid: String, remotePath: String, codec: Codec, display: Display, mask: Mask, pidfile: String): CommandResult? {
        return safeExecuteNohup(
            pidfile,
            timeoutConfiguration.video,
            "io", udid, "recordVideo",
            "--codec",
            codec.value,
            "--display",
            display.value,
            "--mask",
            mask.value,
            "--force",
            remotePath
        )
    }
}
