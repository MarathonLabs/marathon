package com.malinskiy.marathon.ios.xcrun.simctl

import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import java.time.Duration

abstract class SimctlService(private val commandExecutor: CommandExecutor) {
    protected val host = commandExecutor.host

    /**
     * nohup requires us to redirect all streams, otherwise ssh transport will just hang
     */
    protected suspend fun safeExecuteNohup(
        pidfile: String,
        timeout: Duration,
        vararg cmd: String
    ): CommandResult? {
        return commandExecutor.safeExecuteNohup(pidfile, timeout, "xcrun", "simctl", *cmd)
    }

    protected suspend fun criticalExec(
        timeout: Duration,
        vararg args: String,
    ): CommandResult {
        return commandExecutor.criticalExecute(timeout, "xcrun", "simctl", *args)
    }

    protected suspend fun criticalExec(
        timeout: Duration,
        env: Map<String, String>,
        vararg args: String,
    ): CommandResult {
        return commandExecutor.criticalExecute(listOf(*args), timeout, TimeoutConfiguration.INFINITE, env, null)
    }


    protected suspend fun safeExecute(timeout: Duration, vararg args: String) =
        commandExecutor.safeExecute(timeout, "xcrun", "simctl", *args)
}
