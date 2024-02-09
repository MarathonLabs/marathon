package com.malinskiy.marathon.apple.ios.bin.xcrun

import com.google.gson.Gson
import com.malinskiy.marathon.apple.ios.bin.xcrun.simctl.Simctl
import com.malinskiy.marathon.apple.ios.bin.xcrun.xcodebuild.Xcodebuild
import com.malinskiy.marathon.apple.ios.bin.xcrun.xcresulttool.Xcresulttool
import com.malinskiy.marathon.apple.ios.cmd.CommandExecutor
import com.malinskiy.marathon.apple.ios.model.Sdk
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration

class Xcrun(
    private val commandExecutor: CommandExecutor,
    configuration: Configuration,
    vendorConfiguration: VendorConfiguration.IOSConfiguration,
    gson: Gson
) {
    private val timeoutConfiguration = vendorConfiguration.timeoutConfiguration

    val simctl = Simctl(commandExecutor, configuration, vendorConfiguration, gson)
    val xcodebuild = Xcodebuild(commandExecutor, configuration, vendorConfiguration, timeoutConfiguration)
    val xcresulttool = Xcresulttool(commandExecutor, timeoutConfiguration)

    suspend fun getSdkPlatformPath(sdk: Sdk): String {
        return commandExecutor.criticalExecute(
            timeoutConfiguration.shell,
            "xcrun",
            "--sdk",
            sdk.value,
            "--show-sdk-platform-path"
        ).combinedStdout.trim()
    }

    suspend fun getSdkVersion(sdk: Sdk): String {
        return commandExecutor.criticalExecute(
            timeoutConfiguration.shell,
            "xcrun",
            "--sdk",
            sdk.value,
            "--show-sdk-version"
        ).combinedStdout.trim()
    }

    /**
     * @return path of xctest tool under the given SDK platform
     */
    suspend fun getXctesttPath(sdk: Sdk): String {
        return "${getSdkPlatformPath(sdk)}/Developer/Library/Xcode/Agents/xctest"
    }
}
