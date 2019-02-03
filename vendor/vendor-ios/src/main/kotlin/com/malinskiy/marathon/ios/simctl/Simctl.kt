package com.malinskiy.marathon.ios.simctl

import com.dd.plist.PropertyListParser
import com.malinskiy.marathon.ios.IOSDevice
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureException
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.ios.simctl.model.SimctlDevice
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceType
import com.malinskiy.marathon.ios.simctl.model.SimctlListDevicesOutput
import java.io.File

class Simctl {
    fun list(device: IOSDevice, gson: Gson): List<SimctlDevice> {
        val output = exec("list --json", device)
        return try {
            gson.fromJson(output, SimctlListDevicesOutput::class.java).devices.devices
        } catch(e: JsonSyntaxException) {
            throw DeviceFailureException(
                DeviceFailureReason.ServicesUnavailable,
                "Error parsing simctl output on device ${device.udid}: $output"
            )
        }
    }

    fun deviceType(device: IOSDevice): String? {
        return exec("getenv ${device.udid} SIMULATOR_VERSION_INFO", device)
                .split(" - ")
                .associate { it.substringBefore(": ") to it.substringAfter(": ").trim() }
                .get("DeviceType")
    }

    fun isRunning(device: IOSDevice): Boolean {
        val output = exec("spawn ${device.udid} launchctl print system | grep com.apple.springboard.services", device)
        return output.contains("M   A   com.apple.springboard.services")
    }

    fun modelIdentifier(device: IOSDevice): String? {
        return exec("getenv ${device.udid} SIMULATOR_MODEL_IDENTIFIER", device)
                .trim()
                .takeIf { it.isNotBlank() }
    }

    fun simctlDeviceType(device: IOSDevice): SimctlDeviceType {
        val deviceHome: String = exec("getenv ${device.udid} HOME", device)
                .trim()
                .takeIf { it.isNotBlank() }
                ?: return SimctlDeviceType("Unknown", "Unknown")
        val devicePlist = File(deviceHome).resolveSibling("device.plist")
        val devicePlistContents = device.hostCommandExecutor.execBlocking("cat ${devicePlist.canonicalPath}")
        if (devicePlistContents.exitStatus != 0) {
            return SimctlDeviceType("Unknown", "Unknown")
        }
        val deviceDescriptor = PropertyListParser.parse(devicePlistContents.stdout.toByteArray()).toJavaObject() as Map<*, *>
        if (device.udid != deviceDescriptor["UDID"] as String) {
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

    private fun exec(args: String, device: IOSDevice): String {
        val command = "xcrun simctl $args"
        return device.hostCommandExecutor.execBlocking(command).stdout
    }
}
