package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.exceptions.TestBatchExecutionException
import com.malinskiy.marathon.execution.DevicePoolMessage
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice.IsReady
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class DeviceActor(
    private val devicePoolId: DevicePoolId,
    private val pool: SendChannel<DevicePoolMessage>,
    private val configuration: Configuration,
    val device: Device,
    parent: Job,
    context: CoroutineContext
) :
    Actor<DeviceEvent>(parent = parent, context = context, logger = MarathonLogging.logger("DevicePool[${devicePoolId.name}]_DeviceActor[${device.serialNumber}]")) {

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
            on<DeviceEvent.Terminate> {
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
            validTransition.sideEffect?.let {sideEffect ->
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
                            terminate()
                        } else {
                            returnBatchAnd(batch, "Device ${device.serialNumber} terminated") {
                                terminate()
                            }
                        }
                    }
                }
            }
        }
    }
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

    private var job: Job? = null

    private fun initialize() {
        logger.debug { "initialize ${device.serialNumber}" }
        job = async {
            try {
                withRetry(30, 10000) {
                    if (isActive) {
                        try {
                            device.prepare(configuration)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            logger.debug(e) { "device ${device.serialNumber} initialization failed. Retrying" }
                            throw e
                        }
                    }
                }
                state.transition(DeviceEvent.Complete)
            } catch (e: Exception) {
                logger.error(e) { "Error ${e.message}" }
                state.transition(DeviceEvent.Terminate)
            }
        }
    }

    private fun executeBatch(batch: TestBatch, result: CompletableDeferred<TestBatchResults>) {
        logger.debug { "executeBatch ${device.serialNumber}" }
        job = async {
            try {
                device.execute(configuration, devicePoolId, batch, result)
                state.transition(DeviceEvent.Complete)
            } catch (e: CancellationException) {
                logger.warn(e) { "Device execution has been cancelled" }
                state.transition(DeviceEvent.Terminate)
            } catch (e: DeviceLostException) {
                logger.error(e) { "Critical error during execution" }
                state.transition(DeviceEvent.Terminate)
            } catch (e: TestBatchExecutionException) {
                logger.warn(e) { "Test batch failed execution" }
                pool.send(
                    DevicePoolMessage.FromDevice.ReturnTestBatch(
                        device,
                        batch,
                        "Test batch failed execution:\n${e.stackTraceToString()}"
                    )
                )
                state.transition(DeviceEvent.Complete)
            } catch (e: Throwable) {
                logger.error(e) { "Unknown vendor exception caught. Considering this a recoverable error" }
                pool.send(
                    DevicePoolMessage.FromDevice.ReturnTestBatch(
                        device, batch, "Unknown vendor exception caught. \n" +
                            "${e.stackTraceToString()}"
                    )
                )
                state.transition(DeviceEvent.Complete)
            }
        }
    }

    private fun returnBatchAnd(batch: TestBatch, reason: String, completionHandler: CompletionHandler = {}): Job {
        return launch {
            withContext(NonCancellable) {
                pool.send(DevicePoolMessage.FromDevice.ReturnTestBatch(device, batch, reason))
            }
        }.apply {
            invokeOnCompletion(completionHandler)
        }
    }

    private fun terminate() {
        logger.debug { "terminate ${device.serialNumber}" }
        job?.cancel()
        close()
    }
}

