package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.interactor.StartAdbInteractor
import com.malinskiy.adam.request.device.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.ListDevicesRequest
import com.malinskiy.adam.request.misc.GetAdbServerVersionRequest
import com.malinskiy.adam.transport.vertx.VertxSocketFactory
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.config.vendor.VendorConfiguration
import com.malinskiy.marathon.coroutines.newCoroutineExceptionHandler
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.net.ConnectException
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

const val DEFAULT_WAIT_FOR_DEVICES_SLEEP_TIME = 500L

class AdamDeviceProvider(
    val configuration: Configuration,
    private val testBundleIdentifier: AndroidTestBundleIdentifier,
    private val vendorConfiguration: VendorConfiguration.AndroidConfiguration,
    private val track: Track,
    private val timer: Timer
) : DeviceProvider, CoroutineScope {
    private val devices: MutableMap<String, ProvidedDevice> = ConcurrentHashMap()
    private val logger = MarathonLogging.logger("AdamDeviceProvider")

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()

    private val dispatcher = newFixedThreadPoolContext(vendorConfiguration.threadingConfiguration.bootWaitingThreads, "DeviceMonitor")
    private val installDispatcher = Dispatchers.IO.limitedParallelism(vendorConfiguration.threadingConfiguration.installThreads)
    override val coroutineContext = dispatcher + newCoroutineExceptionHandler(logger)

    private val setupSupervisor = SupervisorJob()
    private var providerJob: Job? = null

    private lateinit var clients: List<AndroidDebugBridgeClient>
    private val socketFactory = VertxSocketFactory(idleTimeout = vendorConfiguration.timeoutConfiguration.socketIdleTimeout.toMillis())
    private val logcatManager: LogcatManager = LogcatManager()
    private lateinit var deviceEventsChannel: ReceiveChannel<Pair<AndroidDebugBridgeClient, List<Device>>>
    private var deviceEventsChannelInitialized = false
    private val deviceEventsChannelMutex = Mutex()
    private val multiServerDeviceStateTracker = MultiServerDeviceStateTracker()

    private suspend fun AndroidDebugBridgeClient.isConnectable(): Boolean {
        try {
            printAdbServerVersion(this)
            return true
        } catch (e: ConnectException) {
            if (host.isLoopbackAddress) {
                //For local adb server we try to start it if it's not reachable
                val success = StartAdbInteractor().execute(androidHome = vendorConfiguration.androidSdk, serverPort = port)
                if (!success) {
                    return false
                }
                return try {
                    printAdbServerVersion(this)
                    true
                } catch (e: ConnectException) {
                    false
                }
            } else {
                return false
            }
        }
    }

    override suspend fun initialize() {
        clients = vendorConfiguration.adbServers.map {
            AndroidDebugBridgeClientFactory().apply {
                host = InetAddress.getByName(it.host)
                port = it.port
                socketFactory = socketFactory
            }.build()
        }.filter {
            val connectable = it.isConnectable()
            if (!connectable) {
                logger.error { "adb server ${it.host}:${it.port} is unavailable" }
            }
            connectable
        }

        if (clients.isEmpty()) {
            throw NoDevicesException("All adb servers are unavailable")
        }

        withTimeoutOrNull(vendorConfiguration.waitForDevicesTimeoutMillis) {
            while (clients.flatMap { it.execute(ListDevicesRequest()) }.isEmpty()) {
                delay(DEFAULT_WAIT_FOR_DEVICES_SLEEP_TIME)
            }
        } ?: throw NoDevicesException("No devices found")

        providerJob = launch {
            /**
             * This allows us to survive `adb kill-server`
             */
            while (isActive) {
                deviceEventsChannelMutex.withLock {
                    deviceEventsChannel = produce {
                        clients.forEach { client ->
                            val deviceChannel = client.execute(AsyncDeviceMonitorRequest(), this)
                            launch {
                                deviceChannel.consumeEach { send(Pair(client, it)) }
                            }
                        }
                    }
                    deviceEventsChannelInitialized = true
                }
                for ((client, currentDeviceList) in deviceEventsChannel) {
                    multiServerDeviceStateTracker.update(client, currentDeviceList).forEach { update ->
                        val serial = update.first
                        val state = update.second
                        when (state) {
                            TrackingUpdate.CONNECTED -> {
                                val device =
                                    AdamAndroidDevice(
                                        client,
                                        multiServerDeviceStateTracker.getTracker(client),
                                        logcatManager,
                                        testBundleIdentifier,
                                        installDispatcher,
                                        serial,
                                        configuration,
                                        vendorConfiguration,
                                        track,
                                        timer,
                                        vendorConfiguration.serialStrategy
                                    )
                                track.trackProviderDevicePreparing(device) {
                                    val job = launch(setupSupervisor) {
                                        device.setup()
                                        channel.send(DeviceProvider.DeviceEvent.DeviceConnected(device))
                                    }
                                    devices[serial] = ProvidedDevice(device, job)
                                }
                            }

                            TrackingUpdate.DISCONNECTED -> {
                                devices[serial]?.let { (device, job) ->
                                    if (job.isActive) {
                                        job.cancelAndJoin()
                                    }
                                    channel.send(DeviceProvider.DeviceEvent.DeviceDisconnected(device))
                                    device.dispose()
                                }
                            }

                            TrackingUpdate.NOTHING_TO_DO -> Unit
                        }
                        if (state != TrackingUpdate.NOTHING_TO_DO) {
                            logger.debug { "Device $serial changed state to $state" }
                        }
                    }
                }
            }
        }
    }

    override suspend fun borrow(): AdamAndroidDevice {
        var availableDevices = devices.filter { it.value.setupJob.isCompleted && !it.value.setupJob.isCancelled }
        while (availableDevices.isEmpty()) {
            delay(200)
            availableDevices = devices.filter { it.value.setupJob.isCompleted && !it.value.setupJob.isCancelled }
        }
        return availableDevices.values.random().device
    }

    private suspend fun printAdbServerVersion(client: AndroidDebugBridgeClient) {
        val adbVersion = client.execute(GetAdbServerVersionRequest())
        logger.debug { "Android Debug Bridge ${client.host}:${client.port}: version $adbVersion" }
    }

    override suspend fun terminate() {
        coroutineContext.cancel()
        setupSupervisor.cancel()
        providerJob?.cancel()
        channel.close()
        deviceEventsChannelMutex.withLock {
            if (deviceEventsChannelInitialized) {
                deviceEventsChannel.cancel()
            }
        }
        logcatManager.close()
        socketFactory.close()
    }

    override fun subscribe() = channel
}

data class ProvidedDevice(
    val device: AdamAndroidDevice,
    val setupJob: Job
)
