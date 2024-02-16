package com.malinskiy.marathon.apple.bin.xcrun.simctl.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.malinskiy.marathon.apple.bin.xcrun.simctl.model.SimctlDevice
import com.malinskiy.marathon.apple.bin.xcrun.simctl.model.SimctlListDevicesOutput
import com.malinskiy.marathon.apple.bin.xcrun.simctl.SimctlService
import com.malinskiy.marathon.apple.cmd.CommandExecutor
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.apple.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.config.vendor.apple.ios.TimeoutConfiguration
import com.malinskiy.marathon.log.MarathonLogging

class DeviceService(
    commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
    private val gson: Gson,
) : SimctlService(commandExecutor = commandExecutor) {
    private val logger = MarathonLogging.logger {}
    
    suspend fun list(): SimctlListDevicesOutput {
        val commandResult = criticalExec(timeout = timeoutConfiguration.shell, "list", "--json")
        if (!commandResult.successful) {
            throw DeviceFailureException(
                DeviceFailureReason.ServicesUnavailable,
                "simctl list errored on ${host.id}: stdout=${commandResult.stdout}, stderr=${commandResult.stderr}, exitCode=${commandResult.exitCode}"
            )
        }

        return try {
            return gson.fromJson(commandResult.combinedStdout, SimctlListDevicesOutput::class.java)
        } catch (e: JsonSyntaxException) {
            throw DeviceFailureException(
                DeviceFailureReason.ServicesUnavailable,
                "Error parsing simctl output on ${host.id}: ${commandResult.stdout}"
            )
        }
    }
    
    suspend fun listDevices(): List<SimctlDevice> {
        return list().devices.devices
    }
}
