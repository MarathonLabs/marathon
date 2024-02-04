package com.malinskiy.marathon.ios.device

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.ios.AppleSimulatorDevice
import com.malinskiy.marathon.ios.AppleTestBundleIdentifier
import com.malinskiy.marathon.ios.bin.AppleBinaryEnvironment
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.FileBridge
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.ios.model.Sdk
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer

class SimulatorFactory(
    private val configuration: Configuration,
    private val vendorConfiguration: VendorConfiguration.IOSConfiguration,
    private val testBundleIdentifier: AppleTestBundleIdentifier,
    private val gson: Gson,
    private val track: Track,
    private val timer: Timer,
) {
    private val logger = MarathonLogging.logger {}
    private val fileManager = FileManager(
        configuration.outputConfiguration.maxPath,
        configuration.outputConfiguration.maxFilename,
        configuration.outputDir
    )

    suspend fun create(
        commandExecutor: CommandExecutor,
        fileBridge: FileBridge,
        udid: String,
    ): AppleSimulatorDevice {
        val bin = AppleBinaryEnvironment(commandExecutor, configuration, vendorConfiguration, gson)
        val simctlDevice = try {
            val simctlDevices = bin.xcrun.simctl.device.listDevices()
            simctlDevices.find { it.udid == udid }?.apply {
                if (isAvailable == false) {
                    throw DeviceFailureException(DeviceFailureReason.InvalidSimulatorIdentifier, "udid $udid is not available")
                }
            } ?: throw DeviceFailureException(DeviceFailureReason.InvalidSimulatorIdentifier, "udid $udid is missing")
        } catch (e: DeviceFailureException) {
            commandExecutor.close()
            throw e
        }

        val device = AppleSimulatorDevice(
            simctlDevice.udid,
            Sdk.IPHONESIMULATOR,
            bin,
            testBundleIdentifier,
            fileManager,
            configuration,
            vendorConfiguration,
            commandExecutor,
            fileBridge,
            track,
            timer
        )
        track.trackProviderDevicePreparing(device) {
            device.setup()
        }
        return device
    }
}
