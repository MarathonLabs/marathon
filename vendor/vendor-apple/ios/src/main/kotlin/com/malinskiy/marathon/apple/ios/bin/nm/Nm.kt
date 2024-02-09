package com.malinskiy.marathon.apple.ios.bin.nm

import com.malinskiy.marathon.apple.ios.cmd.CommandExecutor
import com.malinskiy.marathon.apple.ios.cmd.CommandResult
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import com.malinskiy.marathon.log.MarathonLogging
import java.time.Duration

/**
 * display name list (symbol table)
 */
class Nm(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    private val logger = MarathonLogging.logger {}
    suspend fun list(path: String): List<String> {
        return criticalExec(timeoutConfiguration.shell, path).successfulOrNull()?.stdout
            ?: throw DeviceSetupException("failed to extract symbol table from $path")
    }

    /**
     *
     * -g     Display only global (external) symbols.
     * -U     Don't display undefined symbols.
     * -j     Just display the symbol names (no value or type).
     *
     *
     * @see https://github.com/apple/swift/blob/c4cacd10d275ccceccbc3a9c7bf9159b6599a061/lib/Demangling/NodePrinter.cpp#L1218
     */
    suspend fun swiftTests(path: String) =
        listSymbolsVia(path, "'nm -gUj \"$path\" | xargs -s $MAX_XARGS xcrun swift-demangle | cut -d\\  -f3 | grep -e \"[\\\\.|_]\"test'")

    /**
     * -U     Don't display undefined symbols.
     */
    suspend fun objectiveCTests(path: String) =
        listSymbolsVia(path, "'nm -U \"$path\" | grep \" t \" | cut -d\\  -f3,4 | cut -d \"-\" -f2 | cut -d \"[\" -f2 | cut -d \"]\" -f1 | grep \" test\"'")

    private suspend fun listSymbolsVia(path: String, arg: String): List<String> {
        val result = commandExecutor.safeExecute(timeoutConfiguration.shell, "sh", "-c", arg)
        return if (result?.successful == true) {
            result.stdout
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } else {
            if (result?.combinedStdout?.trim()?.isNotEmpty() == true || result?.combinedStderr?.trim()?.isNotEmpty() == true) {
                logger.warn {
                    """
                failed to extract symbol table from $path
                stdout: ${result.combinedStdout}
                stderr: ${result.combinedStderr}
            """.trimIndent()
                }
            }
            emptyList()
        }
    }

    protected suspend fun criticalExec(
        timeout: Duration,
        vararg args: String,
    ): CommandResult {
        return commandExecutor.criticalExecute(timeout, "nm", *args)
    }

    companion object {
        const val MAX_XARGS = 131072
    }
}

