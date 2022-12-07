package com.malinskiy.marathon.ios.simctl

import com.dd.plist.PropertyListParser
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.malinskiy.marathon.ios.IOSDevice
import com.malinskiy.marathon.ios.cmd.CommandExecutor
import com.malinskiy.marathon.ios.cmd.CommandResult
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.ios.simctl.model.SimctlDevice
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceType
import com.malinskiy.marathon.ios.simctl.model.SimctlListDevicesOutput
import java.io.File

class Simctl(private val commandExecutor: CommandExecutor, private val gson: Gson) {
    fun list(): List<SimctlDevice> {
        val output = exec("list --json").stdout
        return try {
            gson.fromJson(output, SimctlListDevicesOutput::class.java).devices.devices
        } catch (e: JsonSyntaxException) {
            throw DeviceFailureException(
                DeviceFailureReason.ServicesUnavailable,
                "Error parsing simctl output on ${commandExecutor.workerId}: $output"
            )
        }
    }

    fun deviceType(device: IOSDevice): String? {
        return exec("getenv ${device.udid} SIMULATOR_VERSION_INFO").stdout
            .split(" - ")
            .associate { it.substringBefore(": ") to it.substringAfter(": ").trim() }
            .get("DeviceType")
    }

    fun isRunning(udid: String): Boolean {
        val output = exec("spawn $udid launchctl print system | grep com.apple.springboard.services").stdout
        return output.contains("M   A   com.apple.springboard.services")
    }

    fun modelIdentifier(udid: String): String? {
        return exec("getenv $udid SIMULATOR_MODEL_IDENTIFIER").stdout
            .trim()
            .takeIf { it.isNotBlank() }
    }

    fun simctlDeviceType(udid: String): SimctlDeviceType {
        val deviceHome: String = exec("getenv $udid HOME").stdout
            .trim()
            .takeIf { it.isNotBlank() }
            ?: return SimctlDeviceType("Unknown", "Unknown")
        val devicePlist = File(deviceHome).resolveSibling("device.plist")
        val devicePlistContents = commandExecutor.execBlocking("cat ${devicePlist.canonicalPath}")
        if (devicePlistContents.exitCode != 0) {
            return SimctlDeviceType("Unknown", "Unknown")
        }
        val deviceDescriptor = PropertyListParser.parse(devicePlistContents.stdout.toByteArray()).toJavaObject() as Map<*, *>
        if (udid != deviceDescriptor["UDID"] as String) {
            return SimctlDeviceType("Unknown", "Unknown")
        }
        val deviceType = deviceDescriptor["deviceType"] as String
        return SimctlDeviceType(deviceType, deviceType)
    }

//    fun boot(device: IOSDevice) {}
//    fun shutdown(device: IOSDevice) {}
//    fun erase(device: IOSDevice) {}
//    fun screenshot(device: IOSDevice) {}
//    fun video(device: IOSDevice) {}
    
    private fun exec(args: String): CommandResult {
        val command = "xcrun simctl $args"
        return commandExecutor.execBlocking(command)
    }
}
