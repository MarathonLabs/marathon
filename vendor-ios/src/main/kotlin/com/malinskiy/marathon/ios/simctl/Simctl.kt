package com.malinskiy.marathon.ios.simctl

import com.dd.plist.PropertyListParser
import com.malinskiy.marathon.ios.IOSDevice
import com.google.gson.Gson
import com.malinskiy.marathon.ios.simctl.model.SimctlDevice
import com.malinskiy.marathon.ios.simctl.model.SimctlDeviceType
import com.malinskiy.marathon.ios.simctl.model.SimctlListDevicesOutput
import java.io.BufferedReader
import java.io.File


class Simctl {
    fun list(device: IOSDevice, gson: Gson): List<SimctlDevice> {
        val output = exec("list --json", device)
        val deviceList = gson.fromJson(output, SimctlListDevicesOutput::class.java)
        return deviceList.devices.devices
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

//    fun boot(device: IOSDevice) {}
//    fun shutdown(device: IOSDevice) {}
//    fun erase(device: IOSDevice) {}

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
        val devicePlistContents = device.hostCommandExecutor.exec("cat ${devicePlist.canonicalPath}")
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

//    fun screenshot(device: IOSDevice) {}
//    fun video(device: IOSDevice) {}

    fun exec(args: String, device: IOSDevice): String {
        val session = device.hostCommandExecutor.startSession()
        val command = session.exec("/Applications/Xcode.app/Contents/Developer/usr/bin/simctl $args")
        command.join()
        val output = command.inputStream.reader().buffered().use(BufferedReader::readText)
        command.close()
        return output
    }
}
