package com.malinskiy.marathon.ios.xcrun.simctl.service

import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.xcrun.simctl.SimctlService
import com.malinskiy.marathon.log.MarathonLogging

class SimulatorService(
    commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) : SimctlService(commandExecutor) {
    private val logger = MarathonLogging.logger {}
    suspend fun boot(udid: String, env: Map<String, String> = emptyMap()): Boolean {
        return criticalExec(
            timeout = timeoutConfiguration.boot,
            env = env.mapKeys { "SIMCTL_CHILD_$it" },
            "boot", udid
        ).successful
    }

    suspend fun getenv(udid: String, key: String): String? {
        return safeExecute(timeoutConfiguration.shell, "getenv", udid, key)
            ?.successfulOrNull()
            ?.combinedStdout
            ?.trim()
    }

    suspend fun monitorStatus(udid: String): Boolean {
        return criticalExec(
            timeout = timeoutConfiguration.boot,
            "bootstatus", udid
        ).successful
    }

    suspend fun shutdown(udid: String): Boolean {
        return criticalExec(
            timeout = timeoutConfiguration.shutdown,
            "shutdown", udid
        ).successful
    }

    /**
     * Simulator should be in the state SHUTDOWN
     */
    suspend fun delete(udid: String): Boolean {
        return criticalExec(
            timeout = timeoutConfiguration.shutdown,
            "delete", udid
        ).successful
    }

    suspend fun eraseAll(): Boolean {
        return criticalExec(
            timeout = timeoutConfiguration.erase,
            "erase", "all"
        ).successful
    }

    suspend fun erase(udid: String) = erase(listOf(udid))

    suspend fun erase(udids: List<String>): Boolean {
        return criticalExec(
            timeout = timeoutConfiguration.erase,
            "erase", *udids.toTypedArray()
        ).successful
    }

    suspend fun isRunning(udid: String, runtimeVersion: String): Boolean {
        val majorVersion = runtimeVersion.substringBefore('.').toIntOrNull()
        val commandResult = when {
            majorVersion == null || majorVersion < 16 -> {
                val commandResult = safeExecute(
                    timeout = timeoutConfiguration.shell,
                    "spawn", udid, "launchctl", "print", "system"
                )
                if (commandResult?.successful != true) {
                    return false
                }
                commandResult
            }

            majorVersion >= 16 -> {
                //Not sure if it's really 16+
                safeExecute(
                    timeout = timeoutConfiguration.shell,
                    "spawn", udid, "launchctl", "dumpstate", "user/foreground/com.apple.SpringBoard"
                )
            }

            else -> {
                logger.warn { "unknown runtime version $runtimeVersion: can't reliably detect if simulator has fully booted" }
                return false
            }
        } ?: return false
        return commandResult.stdout.filter { it.contains("com.apple.springboard.services") }.map {
            it.substringBefore("com.apple.springboard.services")
                .split(" ")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .lastOrNull()
        }.filterNotNull().any { state ->
            /**
             * A -> started
             * D -> starting
             */
            state == "A"
        }
    }
}
