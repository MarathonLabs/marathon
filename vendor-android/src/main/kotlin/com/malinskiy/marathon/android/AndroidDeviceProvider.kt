package com.malinskiy.marathon.android

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.device.DeviceProvider.DeviceEvent.*
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.lang.Thread.sleep
import java.nio.file.Paths

class AndroidDeviceProvider : DeviceProvider {
    private lateinit var adb: AndroidDebugBridge

    val channel = Channel<DeviceProvider.DeviceEvent>(50)

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

        val listener = object : AndroidDebugBridge.IDeviceChangeListener {
            override fun deviceChanged(device: IDevice?, changeMask: Int) {
            }

            override fun deviceConnected(device: IDevice?) {
                device?.let {
                    launch {
                        channel.send(DeviceConnected(AndroidDevice(it)))
                    }
                }
            }

            override fun deviceDisconnected(device: IDevice?) {
                device?.let {
                    launch {
                        channel.send(DeviceDisconnected(AndroidDevice(it)))
                    }
                }
            }
        }
        AndroidDebugBridge.addDeviceChangeListener(listener)
    }

    override fun finish() {
      AndroidDebugBridge.disconnectBridge()
      AndroidDebugBridge.terminate()
    }

    override fun subscribe() = channel

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