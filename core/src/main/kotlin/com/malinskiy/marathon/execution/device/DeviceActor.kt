package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.DevicePoolMessage
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice.RequestNextBatch
import com.malinskiy.marathon.execution.RetryMessage
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import mu.KotlinLogging
import kotlin.properties.Delegates

class DeviceActor(private val devicePoolId: DevicePoolId,
                  private val pool: SendChannel<DevicePoolMessage>,
                  private val configuration: Configuration,
                  private val device: Device,
                  private val analytics: Analytics,
                  private val retry: Channel<RetryMessage>,
                  private val progressReporter: ProgressReporter) : Actor<DeviceEvent>() {


    private val state = StateMachine.create<DeviceState, DeviceEvent, DeviceAction> {
        initialState(DeviceState.Connected)
        state<DeviceState.Connected> {
            on<DeviceEvent.Initialize> {
                transitionTo(DeviceState.Initializing, DeviceAction.Initialize)
            }
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate())
            }
        }
        state<DeviceState.Initializing> {
            on<DeviceEvent.Complete> {
                transitionTo(DeviceState.Ready, DeviceAction.RequestNextBatch)
            }
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate())
            }
        }
        state<DeviceState.Ready> {
            on<DeviceEvent.Execute> {
                transitionTo(DeviceState.Running(it.batch), DeviceAction.ExecuteBatch(it.batch))
            }
            on<DeviceEvent.WakeUp> {
                transitionTo(DeviceState.Ready, DeviceAction.RequestNextBatch)
            }
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate())
            }
        }
        state<DeviceState.Running> {
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate())
            }
            on<DeviceEvent.Complete> {
                transitionTo(DeviceState.Ready, DeviceAction.RequestNextBatch)
            }
        }
        state<DeviceState.Terminated> {
            on<DeviceEvent.Complete> {
                dontTransition()
            }
        }
        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid
            if (validTransition !is StateMachine.Transition.Valid) {
                logger.error { "from ${it.fromState} event ${it.event}" }
                return@onTransition
            }
            val sideEffect = validTransition.sideEffect
            logger.debug { "from ${it.fromState} event ${it.event}" }
            when (sideEffect) {
                DeviceAction.Initialize -> {
                    initialize()
                }
                DeviceAction.RequestNextBatch -> {
                    requestNextBatch()
                }
                is DeviceAction.ExecuteBatch -> {
                    executeBatch(sideEffect.batch)
                }
                is DeviceAction.Terminate -> {
                    val batch = sideEffect.batch
                    batch?.let {
                        returnBatch(it)
                    }
                    terminate()
                }
            }
        }
    }
    private val logger = KotlinLogging.logger("DevicePool[${devicePoolId.name}]_DeviceActor[${device.serialNumber}]")

    override suspend fun receive(msg: DeviceEvent) {
        when (msg) {
            is DeviceEvent.GetDeviceState -> {
                msg.deferred.complete(state.state)
            }
            else -> {
                state.transition(msg)
            }
        }
    }

    private fun requestNextBatch() {
        launch {
            pool.send(RequestNextBatch(device))
        }
    }

    private val context = newSingleThreadContext(device.toString())

    private var job by Delegates.observable<Job?>(null) { _, _, newValue ->
        newValue?.invokeOnCompletion {
            if (it == null) {
                state.transition(DeviceEvent.Complete)
            } else {
                logger.error { it }
                state.transition(DeviceEvent.Terminate)
                terminate()
            }
        }
    }

    private fun initialize() {
        logger.debug { "initialize" }
        job = async(context) {
            device.prepare(configuration)
        }
    }

    private fun executeBatch(batch: TestBatch) {
        logger.debug { "executeBatch" }
        job = async(context) {
            device.execute(configuration, devicePoolId, batch, analytics, retry, progressReporter)
        }
    }

    private fun returnBatch(batch: TestBatch) {
        launch {
            retry.send(RetryMessage.ReturnTestBatch(devicePoolId, batch, device))
        }
    }

    private fun terminate() {
        logger.debug { "terminate" }
        job?.cancel()
        context.close()
        close()
    }
}

