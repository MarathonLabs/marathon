package com.malinskiy.marathon.apple.bin

import com.google.gson.Gson
import com.malinskiy.marathon.apple.bin.codesign.Codesign
import com.malinskiy.marathon.apple.bin.getconf.Getconf
import com.malinskiy.marathon.apple.bin.lipo.Lipo
import com.malinskiy.marathon.apple.bin.nm.Nm
import com.malinskiy.marathon.apple.bin.plistbuddy.PlistBuddy
import com.malinskiy.marathon.apple.bin.xcodeselect.Xcodeselect
import com.malinskiy.marathon.apple.bin.xcrun.Xcrun
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration

class AppleBinaryEnvironment(
    commandExecutor: CommandExecutor,
    configuration: Configuration,
    vendorConfiguration: VendorConfiguration.IOSConfiguration,
    gson: Gson
) {
    private val timeoutConfiguration = vendorConfiguration.timeoutConfiguration

    val codesign: Codesign = Codesign(commandExecutor, timeoutConfiguration)
    val getconf: Getconf =
        Getconf(commandExecutor, timeoutConfiguration)
    val lipo: Lipo = Lipo(commandExecutor, timeoutConfiguration)
    val nm: Nm = Nm(commandExecutor, timeoutConfiguration)
    val plistBuddy = PlistBuddy(commandExecutor, timeoutConfiguration)
    val xcodeselect: Xcodeselect = Xcodeselect(commandExecutor, timeoutConfiguration)
    val xcrun: Xcrun = Xcrun(commandExecutor, configuration, vendorConfiguration, gson)
}
