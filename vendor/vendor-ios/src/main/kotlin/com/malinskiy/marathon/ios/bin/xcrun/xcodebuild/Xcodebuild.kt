package com.malinskiy.marathon.ios.bin.xcrun.xcodebuild

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandSession
import com.malinskiy.marathon.ios.model.XcodeVersion
import com.malinskiy.marathon.ios.test.TestRequest
import com.malinskiy.marathon.log.MarathonLogging
import java.time.Duration

/**
 * build Xcode projects and workspaces
 */
class Xcodebuild(
    private val commandExecutor: CommandExecutor,
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    private val logger = MarathonLogging.logger {}

    suspend fun testWithoutBuilding(udid: String, request: TestRequest): CommandSession {
        val args = mutableMapOf<String, String>().apply {
            putAll(vendorConfiguration.xcodebuildTestArgs)
            put("-enableCodeCoverage", codeCoverageFlag(request))
            put("-resultBundlePath", request.xcresult)
            put("-destination-timeout", timeoutConfiguration.testDestination.seconds.toString())
            put("-destination", "\'platform=iOS simulator,id=$udid\'")
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
