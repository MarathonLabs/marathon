package com.malinskiy.marathon.android

import com.android.ddmlib.AndroidDebugBridge
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.lang.Thread.sleep
import java.nio.file.Paths

class AndroidDeviceProvider : DeviceProvider {

    private lateinit var adb: AndroidDebugBridge

    override fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is AndroidConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration passed")
        }

        AndroidDebugBridge.initIfNeeded(false)

        val absolutePath = Paths.get(vendorConfiguration.androidSdk.absolutePath, "platform-tools", "adb").toFile().absolutePath
        adb = AndroidDebugBridge.createBridge(absolutePath, false)

        var timeout = vendorConfiguration.adbInitTimeoutMillis
        val sleepTime = 1_000
        while (!adb.hasInitialDeviceList() && timeout > 0) {
            sleep(sleepTime.toLong())
            timeout -= sleepTime
        }

        if (timeout <= 0 && !adb.hasInitialDeviceList()) {
            throw RuntimeException("Timeout getting device list.", null)
        }
    }

    override fun getDevices(): List<Device> {
        return adb.devices.map {
            AndroidDevice(it)
        }
    }

    override fun lockDevice(device: Device): Boolean {
        TODO("not implemented")
    }

    override fun unlockDevice(device: Device): Boolean {
        TODO("not implemented")
    }
}