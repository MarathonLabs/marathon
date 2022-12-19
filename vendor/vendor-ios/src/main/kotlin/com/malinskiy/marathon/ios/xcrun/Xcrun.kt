package com.malinskiy.marathon.ios.xcrun

import com.google.gson.Gson
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.xcrun.simctl.Simctl
import com.malinskiy.marathon.ios.xcrun.xcodebuild.Xcodebuild

class Xcrun(
    commandExecutor: CommandExecutor,
    configuration: Configuration,
    vendorConfiguration: VendorConfiguration.IOSConfiguration,
    gson: Gson
) {
    val simctl = Simctl(commandExecutor, configuration, vendorConfiguration, gson)
    val xcodebuild = Xcodebuild(commandExecutor, configuration)
}
