package com.malinskiy.marathon.apple.bin.xcrun.xcodebuild

import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.cmd.CommandSession
import com.malinskiy.marathon.apple.model.Platform
import com.malinskiy.marathon.apple.model.Sdk
import com.malinskiy.marathon.apple.model.XcodeVersion
import com.malinskiy.marathon.apple.test.TestRequest
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration
import com.malinskiy.marathon.log.MarathonLogging
import java.time.Duration

/**
 * build Xcode projects and workspaces
 */
class Xcodebuild(
    private val commandExecutor: CommandExecutor,
    private val configuration: Configuration,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    private val logger = MarathonLogging.logger {}

    suspend fun testWithoutBuilding(udid: String, sdk: Sdk, request: TestRequest, xcodebuildTestArgs: Map<String, String>): CommandSession {
        val args = mutableMapOf<String, String>().apply {
            putAll(xcodebuildTestArgs)
            put("-enableCodeCoverage", codeCoverageFlag(request))
            request.xcresult?.let { put("-resultBundlePath", it) }
            put("-destination-timeout", timeoutConfiguration.testDestination.seconds.toString())
            put("-destination", "\'platform=${sdk.destination},arch=arm64,id=$udid\'")
        }
            .filterKeys { it != "-xctestrun" }
            .toList()
            .flatMap {
                if (it.second.isNotEmpty()) {
                    listOf(it.first, it.second)
                } else {
                    listOf(it.first)
                }
            }

        val command = listOf(
            "xcrun", "xcodebuild", "test-without-building",
            "-xctestrun", request.remoteXctestrun,
            *request.toXcodebuildTestFilter(),
            *args.toTypedArray(),
        )
        logger.debug { "Running ${command.joinToString(" ")}" }
        return commandExecutor.execute(
            command = command,
            timeout = Duration.ofMillis(configuration.testBatchTimeoutMillis),
            idleTimeout = Duration.ofMillis(configuration.testOutputTimeoutMillis),
            workdir = request.workdir,
            env = mapOf("NSUnbufferedIO" to "YES"),
        )
    }

    /**
     * 
     */
    suspend fun getVersion(): XcodeVersion {
        val output = commandExecutor.criticalExecute(timeoutConfiguration.shell, "xcrun", "xcodebuild", "-version").stdout
        /**
         * Xcode 14.2
         * Build version 14C18
         */
        val versionString = output.first().split(" ").last()
        return XcodeVersion.from(versionString)
    }
    
    

    private fun codeCoverageFlag(request: TestRequest) = when (request.coverage) {
        true -> "YES"
        false -> "NO"
    }
}
