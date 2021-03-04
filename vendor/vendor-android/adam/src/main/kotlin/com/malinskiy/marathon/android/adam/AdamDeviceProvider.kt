package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.interactor.StartAdbInteractor
import com.malinskiy.adam.request.device.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.ListDevicesRequest
import com.malinskiy.adam.request.misc.GetAdbServerVersionRequest
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.exception.AdbStartException
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.net.ConnectException
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

private const val DEFAULT_WAIT_FOR_DEVICES_SLEEP_TIME = 500L

class AdamDeviceProvider(
    private val configuration: Configuration,
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

    override val deviceInitializationTimeoutMillis: Long = configuration.deviceInitializationTimeoutMillis

    private lateinit var client: AndroidDebugBridgeClient
    private lateinit var deviceEventsChannel: ReceiveChannel<List<Device>>
    private val deviceEventsChannelMutex = Mutex()
    private val deviceStateTracker = DeviceStateTracker()

    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is AndroidConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration passed")
        }

        client = AndroidDebugBridgeClientFactory().apply {
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

        withTimeoutOrNull(vendorConfiguration.waitForDevicesTimeoutMillis) {
            while (client.execute(ListDevicesRequest()).isEmpty()) {
                delay(DEFAULT_WAIT_FOR_DEVICES_SLEEP_TIME)
            }
        } ?: throw NoDevicesException("No devices found")

        bootWaitContext.executor.execute {
            runBlocking {
                /**
                 * This allows us to survive `adb kill-server`
                 */
                while (isActive) {
                    deviceEventsChannelMutex.withLock {
                        deviceEventsChannel = client.execute(AsyncDeviceMonitorRequest(), this)
                    }
                    for (currentDeviceList in deviceEventsChannel) {
                        deviceStateTracker.update(currentDeviceList).forEach { update ->
                            val serial = update.first
                            val state = update.second
                            when (state) {
                                TrackingUpdate.CONNECTED -> {
                                    val device =
                                        AdamAndroidDevice(
                                            client,
                                            deviceStateTracker,
                                            serial,
                                            configuration,
                                            vendorConfiguration,
                                            track,
                                            timer,
                                            vendorConfiguration.serialStrategy
                                        )
                                    track.trackProviderDevicePreparing(device) {
                                        device.setup()
                                    }
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
    }

    private suspend fun printAdbServerVersion() {
        val adbVersion = client.execute(GetAdbServerVersionRequest())
        logger.debug { "Android Debug Bridge version $adbVersion" }
    }

    override suspend fun terminate() {
        bootWaitContext.close()
        channel.close()
        deviceEventsChannelMutex.withLock {
            deviceEventsChannel.cancel()
        }
    }

    override fun subscribe() = channel
}
