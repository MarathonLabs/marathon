package com.malinskiy.marathon.ios.simctl

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.malinskiy.marathon.config.vendor.ios.Display
import com.malinskiy.marathon.config.vendor.ios.Mask
import com.malinskiy.marathon.config.vendor.ios.TimeoutConfiguration
import com.malinskiy.marathon.config.vendor.ios.Type
import com.malinskiy.marathon.ios.RemoteFileManager
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.cmd.remote.CommandSession
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.ios.simctl.model.SimctlDevice
import com.malinskiy.marathon.ios.simctl.model.SimctlListDevicesOutput
import com.malinskiy.marathon.log.MarathonLogging

class Simctl(private val commandExecutor: CommandExecutor, private val timeoutConfiguration: TimeoutConfiguration, private val gson: Gson) {
    private val logger = MarathonLogging.logger {}

    suspend fun list(): List<SimctlDevice> {
        val commandResult = exec("list --json")
        if (commandResult.exitCode != 0) {
            throw DeviceFailureException(
                DeviceFailureReason.ServicesUnavailable,
                "simctl list errored on ${commandExecutor.workerId}: stdout=${commandResult.stdout}, stderr=${commandResult.stderr}, exitCode=${commandResult.exitCode}"
            )
        }

        return try {
            val listOutput: SimctlListDevicesOutput = gson.fromJson(commandResult.stdout, SimctlListDevicesOutput::class.java)
            listOutput.devices.devices
        } catch (e: JsonSyntaxException) {
            throw DeviceFailureException(
                DeviceFailureReason.ServicesUnavailable,
                "Error parsing simctl output on ${commandExecutor.workerId}: ${commandResult.stdout}"
            )
        }
    }

    suspend fun boot(udid: String): Boolean {
        return exec("boot $udid").exitCode == 0
    }

    suspend fun shutdown(udid: String): Boolean {
        return exec("shutdown $udid").exitCode == 0
    }

    suspend fun eraseAll(): Boolean {
        return exec("erase all").exitCode == 0
    }

    suspend fun erase(udids: List<String>): Boolean {
        return exec("erase ${udids.joinToString(" ")}").exitCode == 0
    }

    private suspend fun exec(args: String): CommandResult {
        val command = "xcrun simctl $args"
        return commandExecutor.execBlocking(command)
    }

    suspend fun screenshot(udid: String, destination: String, type: Type, display: Display, mask: Mask): Boolean {
        return exec("io $udid screenshot --type=${type.value} --display=${display.value} --mask=${mask.value} $destination").exitCode == 0
    }

    suspend fun getenv(udid: String, key: String): String? {
        val commandResult = exec("getenv $udid $key")
        if (commandResult.exitCode != 0) {
            return null
        }
        return commandResult.stdout.trim().ifBlank { null }
    }

    fun recordVideo(udid: String, remotePath: String): CommandSession {
        val command = "xcrun simctl io $udid recordVideo $remotePath"
        return commandExecutor.startSession(command)
    }
}
