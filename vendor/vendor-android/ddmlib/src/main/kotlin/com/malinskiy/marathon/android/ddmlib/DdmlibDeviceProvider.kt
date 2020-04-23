package com.malinskiy.marathon.android.ddmlib

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.DdmPreferences
import com.android.ddmlib.IDevice
import com.android.ddmlib.TimeoutException
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.device.DeviceProvider.DeviceEvent.DeviceConnected
import com.malinskiy.marathon.device.DeviceProvider.DeviceEvent.DeviceDisconnected
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.coroutines.CoroutineContext

private const val DEFAULT_DDM_LIB_TIMEOUT = 30000
private const val DEFAULT_DDM_LIB_SLEEP_TIME = 500

class DdmlibDeviceProvider(
    private val track: Track,
    private val timer: Timer
) : DeviceProvider, CoroutineScope {
    private val logger = MarathonLogging.logger("AndroidDeviceProvider")

    private lateinit var adb: AndroidDebugBridge

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    private val devices: ConcurrentMap<String, DdmlibAndroidDevice> = ConcurrentHashMap()
    private val bootWaitContext = newFixedThreadPoolContext(4, "AndroidDeviceProvider-BootWait")
    override val coroutineContext: CoroutineContext
        get() = bootWaitContext

    override val deviceInitializationTimeoutMillis: Long = 180_000
    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is AndroidConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration passed")
        }
        DdmPreferences.setTimeOut(DEFAULT_DDM_LIB_TIMEOUT)
        AndroidDebugBridge.initIfNeeded(false)

        val absolutePath = Paths.get(vendorConfiguration.androidSdk.absolutePath, "platform-tools", "adb").toFile().absolutePath

        val listener = object : AndroidDebugBridge.IDeviceChangeListener {
            override fun deviceChanged(device: IDevice?, changeMask: Int) {
                device?.let {
                    launch(context = bootWaitContext) {
                        val maybeNewAndroidDevice =
                            DdmlibAndroidDevice(it, device.serialNumber, track, timer, vendorConfiguration.serialStrategy)
                        val healthy = maybeNewAndroidDevice.healthy

                        logger.debug { "Device ${device.serialNumber} changed state. Healthy = $healthy" }
                        if (healthy) {
                            verifyBooted(maybeNewAndroidDevice)
                            val androidDevice = getDeviceOrPut(maybeNewAndroidDevice)
                            notifyConnected(androidDevice)
                        } else {
                            //This shouldn't have any side effects even if device was previously removed
                            notifyDisconnected(maybeNewAndroidDevice)
                        }
                    }
                }
            }

            override fun deviceConnected(device: IDevice?) {
                device?.let {
                    launch {
                        val maybeNewAndroidDevice =
                            DdmlibAndroidDevice(it, device.serialNumber, track, timer, vendorConfiguration.serialStrategy)
                        val healthy = maybeNewAndroidDevice.healthy
                        logger.debug("Device ${device.serialNumber} connected. Healthy = $healthy")

                        if (healthy) {
                            verifyBooted(maybeNewAndroidDevice)
                            val androidDevice = getDeviceOrPut(maybeNewAndroidDevice)
                            notifyConnected(androidDevice)
                        }
                    }
                }
            }

            override fun deviceDisconnected(device: IDevice?) {
                device?.let {
                    launch {
                        logger.debug { "Device ${device.serialNumber} disconnected" }
                        matchDdmsToDevice(it)?.let {
                            notifyDisconnected(it)
                            it.dispose()
                        }
                    }
                }
            }

            private suspend fun verifyBooted(device: DdmlibAndroidDevice) {
                device.setup()
            }

            private fun notifyConnected(device: DdmlibAndroidDevice) {
                launch {
                    channel.send(DeviceConnected(device))
                }
            }

            private fun notifyDisconnected(device: DdmlibAndroidDevice) {
                launch {
                    channel.send(DeviceDisconnected(device))
                }
            }
        }
        AndroidDebugBridge.addDeviceChangeListener(listener)
        adb = AndroidDebugBridge.createBridge(absolutePath, false)

        var getDevicesCountdown = DEFAULT_DDM_LIB_TIMEOUT
        val sleepTime = DEFAULT_DDM_LIB_SLEEP_TIME
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

    private fun getDeviceOrPut(androidDevice: DdmlibAndroidDevice): DdmlibAndroidDevice {
        val newAndroidDevice = devices.getOrPut(androidDevice.serialNumber) {
            androidDevice
        }
        if (newAndroidDevice != androidDevice) {
            androidDevice.dispose()
        }

        return newAndroidDevice
    }

    private fun matchDdmsToDevice(device: IDevice): DdmlibAndroidDevice? {
        val observedDevices = devices.values
        return observedDevices.findLast {
            device == it.ddmsDevice ||
                    device.serialNumber == it.ddmsDevice.serialNumber
        }
    }

    private fun AndroidDebugBridge.hasDevices(): Boolean = devices.isNotEmpty()

    override suspend fun terminate() {
        AndroidDebugBridge.disconnectBridge()
        AndroidDebugBridge.terminate()
        bootWaitContext.close()
        channel.close()
    }

    override fun subscribe() = channel

}
