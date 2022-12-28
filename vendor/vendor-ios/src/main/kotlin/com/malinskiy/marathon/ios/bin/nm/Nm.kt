package com.malinskiy.marathon.ios.bin.nm

import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.model.Arch
import java.time.Duration

/**
 * display name list (symbol table)
 */
class Nm(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    suspend fun list(path: String): List<String> {
        return criticalExec(timeoutConfiguration.shell, path).successfulOrNull()?.stdout
            ?: throw DeviceSetupException("failed to extract symbol table from $path")
    }

    /**
     * @see https://github.com/apple/swift/blob/c4cacd10d275ccceccbc3a9c7bf9159b6599a061/lib/Demangling/NodePrinter.cpp#L1218
     */
    suspend fun swiftTests(path: String): List<String> {
        /**
         * -g     Display only global (external) symbols.
         * -U     Don't display undefined symbols.
         * -j     Just display the symbol names (no value or type).
         */
        val arg = "'nm -gUj \"$path\" | xargs -s $MAX_XARGS xcrun swift-demangle --compact | grep \"$METHOD_DESCRIPTOR_PREFIX\" | sed -e \"s/$METHOD_DESCRIPTOR_PREFIX //\"'"
        return commandExecutor.criticalExecute(timeoutConfiguration.shell, "sh", "-c", arg).successfulOrNull()?.stdout
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: throw DeviceSetupException("failed to extract symbol table from $path")
    }
    
    suspend fun objectiveCTests(path: String): List<String> {
        // -U     Don't display undefined symbols.
        val arg = "'nm -U \"$path\" | grep \" t \" | cut -d\" \" -f3,4 | cut -d\"-\" -f2 | cut -d\"[\" -f2 | cut -d\"]\" -f1'"
        return commandExecutor.criticalExecute(timeoutConfiguration.shell, "sh", "-c", arg).successfulOrNull()?.stdout
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: throw DeviceSetupException("failed to extract symbol table from $path")
    }

    protected suspend fun criticalExec(
        timeout: Duration,
        vararg args: String,
    ): CommandResult {
        return commandExecutor.criticalExecute(timeout, "nm", *args)
    }

    companion object {
        const val METHOD_DESCRIPTOR_PREFIX = "method descriptor for"
        const val MAX_XARGS = 131072
    }
}

