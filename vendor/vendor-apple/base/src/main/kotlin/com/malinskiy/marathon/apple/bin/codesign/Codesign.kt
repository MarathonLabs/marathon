package com.malinskiy.marathon.apple.bin.codesign

import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandResult
import com.malinskiy.marathon.config.vendor.apple.ios.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.DeviceSetupException
import java.time.Duration

/**
 * Create and manipulate code signatures
 * 
 * 
 */
class Codesign(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    /**
     * Gets the codesign identity which signs the bundle with
     */
    suspend fun getIdentityFor(path: String) = getProperty(path, "Authority")

    /**
     * Gets the development team of the bundle
     */
    suspend fun getDevelopmentTeam(path: String) = getProperty(path, "TeamIdentifier")

    /**
     * Codesigns the bundle
     */
    suspend fun sign(bundlePath: String, entitlementsPath: String?, identity: String? = null) {
        val identity = identity ?: getIdentityFor(bundlePath)
        val args = mutableListOf<String>().apply {
            add("codesign")
            add("-f")
            if (entitlementsPath == null) {
                add("--preserve-metadata=identifier,entitlements")
            } else {
                add("--entitlements")
                add(entitlementsPath)
            }
            add("--timestamp=none")
            add("-s")
            add(identity)
            add(bundlePath)
        }.toTypedArray()

        criticalExec(timeoutConfiguration.shell, *args)
    }

    private suspend fun getProperty(path: String, property: String): String {
        val stdout = criticalExec(timeoutConfiguration.shell, "-dvv", path).successfulOrNull()?.stdout
            ?: throw DeviceSetupException("failed to extract $property from $path")
        val authorityLine =
            stdout.find { it.startsWith("$property=") } ?: throw DeviceSetupException("failed to extract $property from $path")
        return authorityLine.substringAfter("$property=")
    }

    protected suspend fun criticalExec(
        timeout: Duration,
        vararg args: String,
    ): CommandResult {
        return commandExecutor.criticalExecute(timeout, "codesign", *args)
    }
}
