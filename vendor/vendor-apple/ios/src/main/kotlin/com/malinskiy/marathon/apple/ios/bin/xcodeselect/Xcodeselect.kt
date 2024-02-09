package com.malinskiy.marathon.apple.ios.bin.xcodeselect

import com.malinskiy.marathon.apple.ios.cmd.CommandExecutor
import com.malinskiy.marathon.apple.ios.model.Sdk
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration

/**
 * Manages the active developer directory for Xcode and BSD tools
 */
class Xcodeselect(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
) {

    /**
     * Gets the active developer path of Xcode command line tools
     */
    suspend fun getDeveloperPath(): String {
        return commandExecutor.criticalExecute(timeoutConfiguration.shell, "xcode-select", "-p").combinedStdout.trim()
    }

    /**
     * From google's xctestrunner:
     *
     * Xcode 11+'s Swift dylibs are configured in a way that does not allow them to
     * load the correct libswiftFoundation.dylib file from
     * libXCTestSwiftSupport.dylib. This bug only affects tests that run on fallbacks
     * to the correct Swift dylibs that have been packaged with Xcode. This method
     * returns the path to that fallback directory.
     * See https://github.com/bazelbuild/rules_apple/issues/684 for context.
     */
    suspend fun getSwift5FallbackLibsDir(): String? {
        val swiftLibsPath = "${getDeveloperPath()}/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift-5.0"
        val swiftLibPlatformDir = "$swiftLibsPath/${Sdk.IPHONESIMULATOR}"
        val result =
            commandExecutor.safeExecute(timeoutConfiguration.shell, "sh", "-c", "'[ -d \"$swiftLibPlatformDir\" ]'")?.successfulOrNull()
                ?: return null

        return if (result.successful) {
            swiftLibPlatformDir
        } else {
            null
        }
    }
}

