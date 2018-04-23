package com.malinskiy.marathon.android

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.device.DeviceProvider.DeviceEvent.*
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.runBlocking
import java.nio.file.Paths

class AndroidDeviceProvider : DeviceProvider {
    private lateinit var adb: AndroidDebugBridge

    private val channel = Channel<DeviceProvider.DeviceEvent>(50)

    override fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is AndroidConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration passed")
        }
        AndroidDebugBridge.initIfNeeded(false)

        val absolutePath = Paths.get(vendorConfiguration.androidSdk.absolutePath, "platform-tools", "adb").toFile().absolutePath

        val listener = object : AndroidDebugBridge.IDeviceChangeListener {
            override fun deviceChanged(device: IDevice?, changeMask: Int) {
            }

            override fun deviceConnected(device: IDevice?) {
                device?.let {
                    println("deviceConnected")
                    runBlocking {
                        channel.send(DeviceConnected(AndroidDevice(it)))
                    }
                }
            }

            override fun deviceDisconnected(device: IDevice?) {
                device?.let {
                    println("deviceDisconnected")
                    runBlocking {
                        channel.send(DeviceDisconnected(AndroidDevice(it)))
                    }
                }
            }
        }
        AndroidDebugBridge.addDeviceChangeListener(listener)
        adb = AndroidDebugBridge.createBridge(absolutePath, false)
    }

    override fun terminate() {
        AndroidDebugBridge.disconnectBridge()
        AndroidDebugBridge.terminate()
    }

    override fun subscribe() = channel

    override fun getDevices(): List<Device> {
        TODO()
//        return adb.devices.map {
//            AndroidDevice(it)
//        }
    }

    override fun lockDevice(device: Device): Boolean {
        TODO("not implemented")
    }

    override fun unlockDevice(device: Device): Boolean {
        TODO("not implemented")
    }
}