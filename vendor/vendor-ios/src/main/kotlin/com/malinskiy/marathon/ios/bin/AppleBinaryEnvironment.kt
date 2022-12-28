package com.malinskiy.marathon.ios.bin

import com.google.gson.Gson
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.ios.bin.codesign.Codesign
import com.malinskiy.marathon.ios.bin.getconf.Getconf
import com.malinskiy.marathon.ios.bin.lipo.Lipo
import com.malinskiy.marathon.ios.bin.nm.Nm
import com.malinskiy.marathon.ios.bin.plistbuddy.PlistBuddy
import com.malinskiy.marathon.ios.bin.xcodeselect.Xcodeselect
import com.malinskiy.marathon.ios.bin.xcrun.Xcrun
import com.malinskiy.marathon.ios.cmd.CommandExecutor

class AppleBinaryEnvironment(
    commandExecutor: CommandExecutor,
    configuration: Configuration,
    vendorConfiguration: VendorConfiguration.IOSConfiguration,
    gson: Gson
) {
    private val timeoutConfiguration = vendorConfiguration.timeoutConfiguration

    val codesign: Codesign = Codesign(commandExecutor, timeoutConfiguration)
    val getconf: Getconf = Getconf(commandExecutor, timeoutConfiguration)
    val lipo: Lipo = Lipo(commandExecutor, timeoutConfiguration)
    val nm: Nm = Nm(commandExecutor, timeoutConfiguration)
    val plistBuddy = PlistBuddy(commandExecutor, timeoutConfiguration)
    val xcodeselect: Xcodeselect = Xcodeselect(commandExecutor, timeoutConfiguration)
    val xcrun: Xcrun = Xcrun(commandExecutor, configuration, vendorConfiguration, gson)
}
