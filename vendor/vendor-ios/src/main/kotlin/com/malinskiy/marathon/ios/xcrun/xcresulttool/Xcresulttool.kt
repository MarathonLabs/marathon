package com.malinskiy.marathon.ios.xcrun.xcresulttool

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.xcrun.simctl.Simctl

/**
 * USAGE: xcresulttool subcommand [options] ...
 *
 * SUBCOMMANDS:
 * export                  Export File or Directory from Result Bundle
 * formatDescription       Result Bundle Format Description
 * get                     Get Result Bundle Object
 * graph                   Print Result Bundle Object Graph
 * merge                   Merge Result Bundles
 * metadata                Result Bundle Metadata
 * version                 XCResultKit Version
 */
class Xcresulttool(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {
    /**
     * Get Result Bundle Object
     *
     * @param path The result bundle path
     * @param format The output format [json|raw], default: raw
     * @param id The ID of the object [optional, assumes rootID if not specified]
     * @param version For incomplete result bundles (lacking Info.plist), specify version explicitly [optional, assumes latest version if not specified]
     */
    suspend fun get(path: String, format: ResultBundleFormat = ResultBundleFormat.JSON, id: String? = null, version: String? = null) {
        val result =
            commandExecutor.criticalExecute(timeoutConfiguration.shell, "xcrun", "xcresulttool", "--path", path, "--format", format.value)
        result.combinedStdout.trim()
    }
}

enum class ResultBundleFormat(val value: String) {
    JSON("json"),
    RAW("raw");
}
