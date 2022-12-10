package com.malinskiy.marathon.ios.device

import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.ios.AppleSimulatorDevice
import com.malinskiy.marathon.ios.configuration.RemoteSimulator
import com.malinskiy.marathon.ios.logparser.parser.DeviceFailureReason
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

private const val MAX_CONNECTION_ATTEMPTS = 16

class AppleSimulatorProvider(
    override val coroutineContext: CoroutineContext,
    override val deviceInitializationTimeoutMillis: Long,
    private val simulatorFactory: SimulatorFactory,
    private val simulators: List<RemoteSimulator>,
) : DeviceProvider, CoroutineScope {
    
    private val logger = MarathonLogging.logger(AppleSimulatorProvider::class.java.simpleName)

    private val job = Job()

    private val devices = ConcurrentHashMap<String, AppleSimulatorDevice>()
    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()

    override fun subscribe() = channel

    override suspend fun initialize() = withContext(coroutineContext) {
        logger.info("starts providing ${simulators.count()} simulator devices")
        simulators.map {
            async(context = coroutineContext) {
                simulatorFactory.createRemote(it, RemoteSimulatorConnectionCounter.putAndGet(it.udid))?.let { connect(it) }
            }
        }.joinAll()
    }

    override suspend fun terminate() = withContext(coroutineContext) {
        logger.info("stops providing anything")
        channel.close()
        if (logger.isDebugEnabled) {
            // print out final summary on attempted simulator connections
            printFailingSimulatorSummary()
        }
        devices.values.forEach {
            dispose(it)
            logger.debug("Disposed device ${it.udid}")
        }
        devices.clear()
    }

    override suspend fun onDisconnect(device: AppleSimulatorDevice) = withContext(coroutineContext + CoroutineName("onDisconnect")) {
        launch(context = coroutineContext + job + CoroutineName("disconnector")) {
            try {
                if (devices.remove(device.serialNumber, device)) {
                    dispose(device)
                    notifyDisconnected(device)
                }
            } catch (e: Exception) {
                logger.debug("Exception removing device ${device.udid}")
            }
        }

        if (device.failureReason == DeviceFailureReason.InvalidSimulatorIdentifier) {
            logger.error("device ${device.udid} does not exist on remote host")
        } else if (RemoteSimulatorConnectionCounter.get(device.udid) < MAX_CONNECTION_ATTEMPTS) {
            launch(context = coroutineContext + job + CoroutineName("reconnector")) {
                delay(499)
                if (isActive) {
                    createDevice(
                        device.simulator,
                        RemoteSimulatorConnectionCounter.putAndGet(device.udid)
                    )?.let {
                        connect(it)
                    }
                }
            }
        }
    }

    private fun dispose(device: AppleSimulatorDevice) {
        device.dispose()
    }

    private fun connect(device: AppleSimulatorDevice) {
        devices.put(device.serialNumber, device)
            ?.let {
                logger.error("replaced existing device $it with new $device.")
                dispose(it)
            }
        notifyConnected(device)
    }

    private fun notifyConnected(device: AppleSimulatorDevice) = launch(context = coroutineContext) {
        channel.send(element = DeviceProvider.DeviceEvent.DeviceConnected(device))
    }

    private fun notifyDisconnected(device: AppleSimulatorDevice) = launch(context = coroutineContext) {
        channel.send(element = DeviceProvider.DeviceEvent.DeviceDisconnected(device))
    }

    private fun printFailingSimulatorSummary() {
        simulators
            .map { "${it.udid}@${it.addr}" to (RemoteSimulatorConnectionCounter.get(it.udid) - 1) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .forEach {
                logger.debug(String.format("%3d %s", it.second, it.first))
            }
    }
}
