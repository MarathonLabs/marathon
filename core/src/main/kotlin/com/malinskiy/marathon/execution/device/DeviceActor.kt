package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.exceptions.DeviceTimeoutException
import com.malinskiy.marathon.exceptions.TestBatchExecutionException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.DevicePoolMessage
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice.IsReady
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates
import kotlin.properties.Delegates.observable

class DeviceActor(private val devicePoolId: DevicePoolId,
                  private val pool: SendChannel<DevicePoolMessage>,
                  private val configuration: Configuration,
                  val device: Device,
                  private val progressReporter: ProgressReporter,
                  parent: Job,
                  context: CoroutineContext) :
        Actor<DeviceEvent>(parent = parent, context = context) {

    private val state = StateMachine.create<DeviceState, DeviceEvent, DeviceAction> {
        initialState(DeviceState.Connected)
        state<DeviceState.Connected> {
            on<DeviceEvent.Initialize> {
                transitionTo(DeviceState.Initializing, DeviceAction.Initialize)
            }
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate)
            }
            on<DeviceEvent.WakeUp> {
                dontTransition()
            }
        }
        state<DeviceState.Initializing> {
            on<DeviceEvent.InitializingComplete> {
                transitionTo(DeviceState.Ready, DeviceAction.NotifyIsReady)
            }
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate)
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
                transitionTo(DeviceState.Ready, DeviceAction.NotifyIsReady)
            }
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate)
            }
        }
        state<DeviceState.Running> {
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.StopAndTerminatee(this.result))
            }
            on<DeviceEvent.RunningComplete> {
                transitionTo(DeviceState.Ready, DeviceAction.SendResultAndNotifyIsReady(this.result))
            }
            on<DeviceEvent.WakeUp> {
                dontTransition()
            }
            on<DeviceEvent.Initialize> {
                transitionTo(DeviceState.Initializing, DeviceAction.ReturnBatchAndInitialize(this.result))
            }
        }
        state<DeviceState.Terminated> {
            on<DeviceEvent.WakeUp> {
                dontTransition()
            }
        }
        onTransition {
            val validTransition = it as? StateMachine.Transition.Valid
            if (validTransition !is StateMachine.Transition.Valid) {
                logger.error { "Invalid transition from ${it.fromState} by event ${it.event}" }
                return@onTransition
            }

            logger.debug("Transition from ${it.fromState.javaClass.simpleName} to ${validTransition.toState} by event ${validTransition.event}")

            val sideEffect = validTransition.sideEffect
            val justForWarning = when (sideEffect) {
                null -> {
                    // вам должно быть стыдно!
                }
                DeviceAction.Initialize -> {
                    initialize()
                }
                is DeviceAction.SendResultAndNotifyIsReady -> {
                    sendResults(sideEffect.result)
                    notifyIsReady()
                }
                is DeviceAction.NotifyIsReady -> {
                    notifyIsReady()
                }
                is DeviceAction.ExecuteBatch -> {
                    executeBatch(sideEffect.batch, sideEffect.result)
                }
                is DeviceAction.Terminate -> {
                    device.forceEnd()
                    terminate()
                }
                is DeviceAction.StopAndTerminatee -> {
                    device.forceEnd()
                    sendResults(sideEffect.result)
                    terminate()
                }
                is DeviceAction.ReturnBatchAndInitialize -> {
                    device.forceEnd()
                    sendResults(sideEffect.result)
                    initialize()

//                    terminate()
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

    private var job by observable<Job?>(null) { _, _, newValue ->
        newValue?.invokeOnCompletion {
            if (newValue.isCancelled) {
                logger.debug("seems like terminating is in progress.")

            } else if (it != null) {
                // job is alive but failed
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

                state.transition(DeviceEvent.InitializingComplete)

            } catch (e: Exception) {
                logger.error(e) { "Critical error during device initialisation" }
                state.transition(DeviceEvent.Terminate)
            }

        }
    }

    private fun executeBatch(batch: TestBatch, result: CompletableDeferred<TestBatchResults>) {
        logger.debug { "executeBatch ${device.serialNumber}" }
        job = async {
            try {
                logger.debug("batch started")
                device.execute(configuration, devicePoolId, batch, result, progressReporter)
                logger.debug("batch finished")

                state.transition(DeviceEvent.RunningComplete)

            } catch (e: DeviceLostException) {
                logger.error(e) { "Critical error during execution" }
                state.transition(DeviceEvent.Terminate)

            } catch (e: DeviceTimeoutException) {
                logger.error(e) { "Critical error during execution" }
                state.transition(DeviceEvent.Initialize) // there is the difference with DeviceLostException: we still have device so we can kick it

            } catch (e: TestBatchExecutionException) {
                logger.error(e) { "Critical error during execution" }
                state.transition(DeviceEvent.RunningComplete)
            }
        }
    }

    private fun terminate() {
        logger.debug { "terminate ${device.serialNumber}" }
        job?.cancel()
        close()
    }
}

