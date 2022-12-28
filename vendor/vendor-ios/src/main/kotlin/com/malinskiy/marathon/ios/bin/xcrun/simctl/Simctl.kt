package com.malinskiy.marathon.ios.bin.xcrun.simctl

import com.google.gson.Gson
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.bin.xcrun.simctl.service.DeviceService
import com.malinskiy.marathon.ios.bin.xcrun.simctl.service.IoService
import com.malinskiy.marathon.ios.bin.xcrun.simctl.service.PrivacyService
import com.malinskiy.marathon.ios.bin.xcrun.simctl.service.SimulatorService
import com.malinskiy.marathon.log.MarathonLogging

class Simctl(
    private val commandExecutor: CommandExecutor,
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val gson: Gson
) {
    private val logger = MarathonLogging.logger {}
    private val timeoutConfiguration: TimeoutConfiguration = vendorConfiguration.timeoutConfiguration
    val device = DeviceService(commandExecutor, timeoutConfiguration, gson)

    val simulator = SimulatorService(commandExecutor, timeoutConfiguration)
    val io = IoService(commandExecutor, timeoutConfiguration)
    val privacy = PrivacyService(commandExecutor, timeoutConfiguration)
}
