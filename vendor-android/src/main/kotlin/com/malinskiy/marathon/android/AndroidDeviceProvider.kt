package com.malinskiy.marathon.android

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.DdmPreferences
import com.android.ddmlib.IDevice
import com.android.ddmlib.TimeoutException
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.device.DeviceProvider.DeviceEvent.DeviceConnected
import com.malinskiy.marathon.device.DeviceProvider.DeviceEvent.DeviceDisconnected
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import mu.KotlinLogging
import java.nio.file.Paths

private const val DEFAULT_DDM_LIB_TIMEOUT = 30000

class AndroidDeviceProvider : DeviceProvider {

    private val logger = KotlinLogging.logger("AndroidDeviceProvider")

    private lateinit var adb: AndroidDebugBridge

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()

    override fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is AndroidConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration passed")
        }
        DdmPreferences.setTimeOut(DEFAULT_DDM_LIB_TIMEOUT)
        AndroidDebugBridge.initIfNeeded(false)

        val absolutePath = Paths.get(vendorConfiguration.androidSdk.absolutePath, "platform-tools", "adb").toFile().absolutePath

        val listener = object : AndroidDebugBridge.IDeviceChangeListener {
            override fun deviceChanged(device: IDevice?, changeMask: Int) {}

            override fun deviceConnected(device: IDevice?) {
                device?.let {
                    logger.debug { "Device ${device.serialNumber} connected channel.isFull = ${channel.isFull}" }
                    launch {
                        channel.send(DeviceConnected(AndroidDevice(it)))
                    }
                }
            }

            override fun deviceDisconnected(device: IDevice?) {
                device?.let {
                    logger.debug { "Device ${device.serialNumber} disconnected" }
                    launch {
                        channel.send(DeviceDisconnected(AndroidDevice(it)))
                    }
                }
            }
        }
        AndroidDebugBridge.addDeviceChangeListener(listener)
        adb = AndroidDebugBridge.createBridge(absolutePath, false)

        var getDevicesCountdown = DEFAULT_DDM_LIB_TIMEOUT
        val sleepTime = 500
        while (!adb.hasInitialDeviceList() || !adb.hasDevices() && getDevicesCountdown >= 0) {
            try {
                Thread.sleep(sleepTime.toLong())
            } catch (e: InterruptedException) {
                throw TimeoutException("Timeout getting device list", e)
            }
            getDevicesCountdown -= sleepTime
        }
        if (!adb.hasInitialDeviceList() || !adb.hasDevices()) {
            throw NoDevicesException("No devices found.")
        }
    }

    private fun AndroidDebugBridge.hasDevices(): Boolean = devices.isNotEmpty()

    override fun terminate() {
        AndroidDebugBridge.disconnectBridge()
        AndroidDebugBridge.terminate()
    }

    override fun subscribe() = channel

    override fun lockDevice(device: Device): Boolean {
        TODO("not implemented")
    }

    override fun unlockDevice(device: Device): Boolean {
        TODO("not implemented")
    }
}
