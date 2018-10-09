package com.malinskiy.marathon.ios.simctl

import com.malinskiy.marathon.ios.IOSDevice
import com.google.gson.Gson
import com.malinskiy.marathon.ios.simctl.model.SimctlDevice
import com.malinskiy.marathon.ios.simctl.model.SimctlListDevicesOutput
import java.io.BufferedReader


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

//    fun boot(device: IOSDevice) {}
//    fun shutdown(device: IOSDevice) {}
//    fun erase(device: IOSDevice) {}

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
