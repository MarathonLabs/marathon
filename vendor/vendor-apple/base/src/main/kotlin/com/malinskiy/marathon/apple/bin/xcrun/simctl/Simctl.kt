package com.malinskiy.marathon.apple.bin.xcrun.simctl

import com.google.gson.Gson
import com.malinskiy.marathon.apple.bin.xcrun.simctl.service.ApplicationService
import com.malinskiy.marathon.apple.bin.xcrun.simctl.service.DeviceService
import com.malinskiy.marathon.apple.bin.xcrun.simctl.service.IoService
import com.malinskiy.marathon.apple.bin.xcrun.simctl.service.MediaService
import com.malinskiy.marathon.apple.bin.xcrun.simctl.service.PrivacyService
import com.malinskiy.marathon.apple.bin.xcrun.simctl.service.SimulatorService
import com.malinskiy.marathon.apple.bin.xcrun.simctl.service.SpawnService
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.config.vendor.apple.TimeoutConfiguration
import com.malinskiy.marathon.log.MarathonLogging

class Simctl(
    private val commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
    private val gson: Gson
) {
    val device = DeviceService(commandExecutor, timeoutConfiguration, gson)

    val simulator = SimulatorService(commandExecutor, timeoutConfiguration)
    val io = IoService(commandExecutor, timeoutConfiguration)
    val privacy = PrivacyService(commandExecutor, timeoutConfiguration)
    val mediaService = MediaService(commandExecutor, timeoutConfiguration)
    val application = ApplicationService(commandExecutor, timeoutConfiguration)
    val spawn = SpawnService(commandExecutor, timeoutConfiguration)
}
