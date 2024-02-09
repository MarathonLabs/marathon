package com.malinskiy.marathon.apple.ios.bin.getconf

import com.malinskiy.marathon.apple.ios.cmd.CommandExecutor
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration

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
