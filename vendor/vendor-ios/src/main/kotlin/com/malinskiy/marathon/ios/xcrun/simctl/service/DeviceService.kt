package com.malinskiy.marathon.ios.xcrun.simctl.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.ios.xcrun.simctl.SimctlService
import com.malinskiy.marathon.ios.xcrun.simctl.model.SimctlDevice
import com.malinskiy.marathon.ios.xcrun.simctl.model.SimctlListDevicesOutput
import com.malinskiy.marathon.log.MarathonLogging

class DeviceService(
    commandExecutor: CommandExecutor,
    private val timeoutConfiguration: TimeoutConfiguration,
    private val gson: Gson,
) : SimctlService(commandExecutor = commandExecutor) {
    private val logger = MarathonLogging.logger {}
    suspend fun list(): List<SimctlDevice> {
        val commandResult = criticalExec(timeout = timeoutConfiguration.shell, "list", "--json")
        if (!commandResult.successful) {
            throw DeviceFailureException(
                DeviceFailureReason.ServicesUnavailable,
                "simctl list errored on ${host.id}: stdout=${commandResult.stdout}, stderr=${commandResult.stderr}, exitCode=${commandResult.exitCode}"
            )
        }

        return try {
            val listOutput: SimctlListDevicesOutput = gson.fromJson(commandResult.combinedStdout, SimctlListDevicesOutput::class.java)
            listOutput.devices.devices
        } catch (e: JsonSyntaxException) {
            throw DeviceFailureException(
                DeviceFailureReason.ServicesUnavailable,
                "Error parsing simctl output on ${host.id}: ${commandResult.stdout}"
            )
        }
    }
}
