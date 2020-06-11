package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.exceptions.DeviceLostException
import com.malinskiy.marathon.exceptions.TestBatchExecutionException
import com.malinskiy.marathon.exceptions.TestBatchTimeoutException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.DevicePoolMessage
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice.IsReady
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
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

    companion object {
        private const val AWAIT_BATCH_RESULTS_TIMEOUT_MS = 500L
    }

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
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate(testBatch, this.result))
            }
            on<DeviceEvent.Complete> {
                transitionTo(DeviceState.Ready, DeviceAction.NotifyIsReady(testBatch, this.result))
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
            when (val sideEffect = validTransition.sideEffect) {
                DeviceAction.Initialize -> {
                    initialize()
                }
                is DeviceAction.NotifyIsReady -> {
                    sendResults(sideEffect.batch, sideEffect.result).invokeOnCompletion {
                        notifyIsReady()
                    }
                }
                is DeviceAction.ExecuteBatch -> {
                    executeBatch(sideEffect.batch, sideEffect.result)
                }
                is DeviceAction.Terminate -> {
                    sendResults(sideEffect.batch, sideEffect.result).invokeOnCompletion {
                        terminate()
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

    private fun sendResults(testBatch: TestBatch?, results: CompletableDeferred<TestBatchResults>?): Job = launch {
        if (results == null) {
            returnBatch(testBatch)
            return@launch
        }

        val innerJob = launch {
            logger.debug("Awaiting batch results to DevicePool: ${device.serialNumber}")
            val testResults = results.await()
            pool.send(DevicePoolMessage.FromDevice.CompletedTestBatch(device, testResults))
            logger.debug("Sent batch results to DevicePool: ${device.serialNumber}")
        }
        delay(AWAIT_BATCH_RESULTS_TIMEOUT_MS)
        if (innerJob.isActive) {
            logger.debug("Cancel awaiting batch results to DevicePool: ${device.serialNumber}")
            innerJob.cancelAndJoin()
            returnBatch(testBatch)
        }
    }

    private suspend fun returnBatch(batch: TestBatch?) {
        if (batch != null) {
            logger.debug { "Return test batch - ${batch.tests.size} tests from ${device.serialNumber}" }
            pool.send(DevicePoolMessage.FromDevice.ReturnTestBatch(device, batch))
        }
    }

    private fun notifyIsReady() = launch {
        pool.send(IsReady(device))
    }

    private var job by Delegates.observable<Job?>(null) { _, _, newValue ->
        newValue?.invokeOnCompletion {
            if (it == null) {
                state.transition(DeviceEvent.Complete)
            } else {
                logger.error(it) { "Error: '${it.message}'" }
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
            } catch (exc: TestBatchTimeoutException) {
                logger.warn { "Critical error during batch execution: batch timed out. ${exc.cause.toString()}" }
                state.transition(DeviceEvent.Terminate)
            } catch (exc: DeviceLostException) {
                logger.error(exc) { "Critical error during batch execution: device is lost" }
                state.transition(DeviceEvent.Terminate)
            } catch (exc: TestBatchExecutionException) {
                logger.error(exc) { "Critical error during batch execution: ${exc.cause.toString()}" }
                state.transition(DeviceEvent.Terminate)
            }
        }
    }

    private fun terminate() {
        logger.debug { "terminate ${device.serialNumber}" }
        job?.cancel()
        close()
    }
}

