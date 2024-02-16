package com.malinskiy.marathon.apple.bin.xcrun.simctl.service

import com.malinskiy.marathon.apple.bin.xcrun.simctl.SimctlService
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.config.vendor.apple.ios.TimeoutConfiguration
import com.malinskiy.marathon.log.MarathonLogging

class SimulatorService(
    commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) : SimctlService(commandExecutor) {
    private val logger = MarathonLogging.logger {}


    /**
     * Create a new device.
     * Usage: simctl create <name> <device type id> [<runtime id>]
     *
     *         <device type id>    A valid available device type. Find these by running "xcrun simctl list devicetypes".
     *                             Examples: ("iPhone X", "com.apple.CoreSimulator.SimDeviceType.iPhone-X")
     *         <runtime id>        A valid and available runtime. Find these by running "xcrun simctl list runtimes".
     *                             If no runtime is specified the newest runtime compatible with the device type is chosen.
     *                             Examples: ("watchOS3", "watchOS3.2", "watchOS 3.2", "com.apple.CoreSimulator.SimRuntime.watchOS-3-2",
     *                                        "/Volumes/path/to/Runtimes/watchOS 3.2.simruntime")
     *
     * Expected output:
     * No runtime specified, using 'iOS 16.2 (16.2 - 20C52) - com.apple.CoreSimulator.SimRuntime.iOS-16-2'
     * FE547FCD-A71D-4B09-BE3C-97C18FD0F667
     */
    suspend fun create(name: String, deviceTypeId: String, runtimeId: String? = null): String? {
        val args = mutableListOf<String>().apply {
            add("create")
            add(name)
            add(deviceTypeId)
            runtimeId?.let { add(it) }
        }
        val result = criticalExec(
            timeout = timeoutConfiguration.create,
            *args.toTypedArray()
        )
        return result.stdout.findLast { it.trim().matches(UDID_REGEX) }
    }

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
            timeout = timeoutConfiguration.delete,
            "delete", udid
        ).successful
    }

    suspend fun deleteUnavailable() = deleteWithFilter("unavailable")

    /**
     * Possible options are all | unavailable
     */
    private suspend fun deleteWithFilter(filter: String): Boolean {
        return criticalExec(
            timeout = timeoutConfiguration.delete,
            "delete", filter
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

    companion object {
        val UDID_REGEX = "[A-Z0-9\\-]*".toRegex()
    }
}
