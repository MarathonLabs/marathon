package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.AndroidDebugBridgeServer
import com.malinskiy.adam.AndroidDebugBridgeServerFactory
import com.malinskiy.adam.interactor.StartAdbInteractor
import com.malinskiy.adam.request.async.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.devices.Device
import com.malinskiy.adam.request.devices.ListDevicesRequest
import com.malinskiy.adam.request.sync.GetAdbServerVersionRequest
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.exception.AdbStartException
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.net.ConnectException
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

private const val DEFAULT_WAIT_FOR_DEVICES_TIMEOUT = 30000L
private const val DEFAULT_WAIT_FOR_DEVICES_SLEEP_TIME = 500L

class AdamDeviceProvider(
    private val track: Track,
    private val timer: Timer
) : DeviceProvider, CoroutineScope {
    private val devices: MutableMap<String, AdamAndroidDevice> = ConcurrentHashMap()
    private val logger = MarathonLogging.logger("AdamDeviceProvider")

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    private val bootWaitContext = newFixedThreadPoolContext(4, "AdamDeviceProvider")
    override val coroutineContext: CoroutineContext
        get() = bootWaitContext

    private val adbCommunicationContext: CoroutineContext by lazy {
        newFixedThreadPoolContext(4, "AdbIOThreadPool")
    }

    override val deviceInitializationTimeoutMillis: Long = 180_000

    private lateinit var server: AndroidDebugBridgeServer
    private lateinit var deviceEventsChannel: ReceiveChannel<List<Device>>
    private val deviceStateTracker = DeviceStateTracker()

    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is AndroidConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration passed")
        }

        server = AndroidDebugBridgeServerFactory().apply {
            coroutineContext = adbCommunicationContext
        }.build()

        try {
            printAdbServerVersion()
        } catch (e: ConnectException) {
            val success = StartAdbInteractor().execute(androidHome = vendorConfiguration.androidSdk)
            if (!success) {
                throw AdbStartException()
            }
            printAdbServerVersion()
        }

        withTimeoutOrNull(DEFAULT_WAIT_FOR_DEVICES_TIMEOUT) {
            while (server.execute(ListDevicesRequest()).isEmpty()) {
                delay(DEFAULT_WAIT_FOR_DEVICES_SLEEP_TIME)
            }
        } ?: throw NoDevicesException("No devices found")

        deviceEventsChannel = server.execute(AsyncDeviceMonitorRequest(), this)
        bootWaitContext.executor.execute {
            runBlocking {
                while (!deviceEventsChannel.isClosedForReceive) {
                    val currentDeviceList = deviceEventsChannel.receive()
                    deviceStateTracker.update(currentDeviceList).forEach { update ->
                        val serial = update.first
                        val state = update.second
                        when (state) {
                            TrackingUpdate.CONNECTED -> {
                                val device =
                                    AdamAndroidDevice(server, deviceStateTracker, serial, track, timer, vendorConfiguration.serialStrategy)
                                device.setup()
                                channel.send(DeviceProvider.DeviceEvent.DeviceConnected(device))
                                devices[serial] = device
                            }
                            TrackingUpdate.DISCONNECTED -> {
                                devices[serial]?.let { device ->
                                    channel.send(DeviceProvider.DeviceEvent.DeviceDisconnected(device))
                                    device.dispose()
                                }
                            }
                            TrackingUpdate.NOTHING_TO_DO -> Unit
                        }
                        logger.debug { "Device $serial changed state to $state" }
                    }
                }
            }
        }
    }

    private suspend fun printAdbServerVersion() {
        val adbVersion = server.execute(GetAdbServerVersionRequest())
        logger.debug { "Android Debug Bridge version $adbVersion" }
    }

    override suspend fun terminate() {
        bootWaitContext.close()
        channel.close()
        deviceEventsChannel.cancel()
    }

    override fun subscribe() = channel
}
