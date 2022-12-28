package com.malinskiy.marathon.ios.bin.getconf

import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor

/**
 * retrieve standard configuration variables
 */
class Getconf(private val commandExecutor: CommandExecutor, private val timeoutConfiguration: TimeoutConfiguration) {

    suspend fun getDarwinUserCacheDir() = get("DARWIN_USER_CACHE_DIR")

    suspend fun get(key: String): String {
        return commandExecutor.criticalExecute(
            timeoutConfiguration.shell,
            "getconf",
            key,
        ).combinedStdout.trim()
    }
}
