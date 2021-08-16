package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.exceptions.TestBatchExecutionException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.DevicePoolMessage
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice.IsReady
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

class DeviceActor(
    private val devicePoolId: DevicePoolId,
    private val pool: SendChannel<DevicePoolMessage>,
    private val configuration: Configuration,
    val device: Device,
    private val progressReporter: ProgressReporter,
    parent: Job,
    context: CoroutineContext
) :
    Actor<DeviceEvent>(parent = parent, context = context) {

    private val state = StateMachine.create<DeviceState, DeviceEvent, DeviceAction> {
        initialState(DeviceState.Connected)
        state<DeviceState.Connected> {
            on<DeviceEvent.Initialize> {
                transitionTo(DeviceState.Initializing, DeviceAction.Initialize)
            }
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate())
            }
            on<DeviceEvent.WakeUp> {
                dontTransition()
            }
        }
        state<DeviceState.Initializing> {
            on<DeviceEvent.Complete> {
                transitionTo(DeviceState.Ready, DeviceAction.NotifyIsReady())
            }
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate())
            }
            on<DeviceEvent.WakeUp> {
                dontTransition()
            }
        }
        state<DeviceState.Ready> {
            on<DeviceEvent.Execute> {
                val deferred = CompletableDeferred<TestBatchResults>()
                transitionTo(DeviceState.Running(it.batch, deferred), DeviceAction.ExecuteBatch(it.batch, deferred))
            }
            on<DeviceEvent.WakeUp> {
                transitionTo(DeviceState.Ready, DeviceAction.NotifyIsReady())
            }
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate())
            }
        }
        state<DeviceState.Running> {
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate(testBatch))
            }
            on<DeviceEvent.Complete> {
                transitionTo(DeviceState.Ready, DeviceAction.NotifyIsReady(this.result))
            }
            on<DeviceEvent.WakeUp> {
                dontTransition()
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
                if (it.event !is DeviceEvent.WakeUp) {
                    logger.error { "Invalid transition from ${it.fromState} event ${it.event}" }
                }
                return@onTransition
            }
            val sideEffect = validTransition.sideEffect
            when (sideEffect) {
                DeviceAction.Initialize -> {
                    initialize()
                }
                is DeviceAction.NotifyIsReady -> {
                    sideEffect.result?.let {
                        sendResults(it)
                    }
                    notifyIsReady()
                }
                is DeviceAction.ExecuteBatch -> {
                    executeBatch(sideEffect.batch, sideEffect.result)
                }
                is DeviceAction.Terminate -> {
                    val batch = sideEffect.batch
                    if (batch == null) {
                        logger.debug { "deviceAction batch == null terminate" }
                        terminate()
                    } else {
                        returnBatch(batch).invokeOnCompletion {
                            logger.debug { "deviceAction batch != null terminate" }
                            terminate()
                        }
                    }
                }
            }
        }
    }
    private val logger = MarathonLogging.logger("DevicePool[${devicePoolId.name}]_DeviceActor[${device.serialNumber}]")

    val isAvailable: Boolean
        get() {
            return !isClosedForSend && state.state == DeviceState.Ready
        }

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

    private fun sendResults(result: CompletableDeferred<TestBatchResults>) {
        launch {
            val testResults = result.await()
            pool.send(DevicePoolMessage.FromDevice.CompletedTestBatch(device, testResults))
        }
    }

    private fun notifyIsReady() {
        launch {
            pool.send(IsReady(device))
        }
    }

    private var job by Delegates.observable<Job?>(null) { _, _, newValue ->
        newValue?.invokeOnCompletion {
            if (it == null) {
                state.transition(DeviceEvent.Complete)
            } else {
                logger.error(it) { "Error ${it.message}" }
                state.transition(DeviceEvent.Terminate)
                terminate()
            }
        }
    }

    private fun initialize() {
        logger.debug { "initialize ${device.serialNumber}" }
        job = async {
            try {
                withRetry(30, 10000) {
                    if (isActive) {
                        try {
                            device.prepare(configuration)
                        } catch (e: Exception) {
                            logger.debug { "device ${device.serialNumber} initialization failed. Retrying" }
                            throw e
                        }
                    }
                }
            } catch (e: Exception) {
                state.transition(DeviceEvent.Terminate)
            }
        }
    }

    private fun executeBatch(batch: TestBatch, result: CompletableDeferred<TestBatchResults>) {
        logger.debug { "executeBatch ${device.serialNumber}" }
        job = async {
            try {
                device.execute(configuration, devicePoolId, batch, result, progressReporter)
            } catch (e: DeviceLostException) {
                logger.error(e) { "Critical error during execution" }
                state.transition(DeviceEvent.Terminate)
            } catch (e: TestBatchExecutionException) {
                returnBatch(batch)
            }
        }
    }

    private fun returnBatch(batch: TestBatch): Job {
        return launch {
            pool.send(DevicePoolMessage.FromDevice.ReturnTestBatch(device, batch))
        }
    }

    private fun terminate() {
        logger.debug { "terminate ${device.serialNumber}" }
        job?.cancel()
        close()
    }
}

