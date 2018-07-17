package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.actor.Actor
import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.DevicePoolMessage
import com.malinskiy.marathon.execution.DevicePoolMessage.FromDevice.RequestNextBatch
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.CompletableDeferred
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import mu.KotlinLogging
import kotlin.properties.Delegates

class DeviceActor(private val devicePoolId: DevicePoolId,
                  private val pool: SendChannel<DevicePoolMessage>,
                  private val configuration: Configuration,
                  private val device: Device,
                  private val progressReporter: ProgressReporter,
                  parent: Job) : Actor<DeviceEvent>(parent = parent) {

    private val deviceJob = Job(parent)

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
                transitionTo(DeviceState.Ready, DeviceAction.RequestNextBatch())
            }
            on<DeviceEvent.Terminate> {
                transitionTo(DeviceState.Terminated, DeviceAction.Terminate())
            }
        }
        state<DeviceState.Ready> {
            on<DeviceEvent.Execute> {
                val deferred = CompletableDeferred<TestBatchResults>()
                transitionTo(DeviceState.Running(it.batch, deferred), DeviceAction.ExecuteBatch(it.batch, deferred))
            }
            on<DeviceEvent.WakeUp> {
                transitionTo(DeviceState.Ready, DeviceAction.RequestNextBatch())
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
                transitionTo(DeviceState.Ready, DeviceAction.RequestNextBatch(this.result))
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
                is DeviceAction.RequestNextBatch -> {
                    requestNextBatch(sideEffect.result)
                }
                is DeviceAction.ExecuteBatch -> {
                    executeBatch(sideEffect.batch, sideEffect.result)
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

    private fun requestNextBatch(result: CompletableDeferred<TestBatchResults>?) {
        launch(parent = deviceJob) {
            if (result != null) {
                val testResults = result.await()
                pool.send(DevicePoolMessage.FromDevice.CompletedTestBatch(device, testResults))
            } else {
                pool.send(RequestNextBatch(device))
            }
        }
    }

    private val context = newSingleThreadContext(device.toString())

    private var job by Delegates.observable<Job?>(null) { _, _, newValue ->
        newValue?.invokeOnCompletion {
            if (it == null) {
                state.transition(DeviceEvent.Complete)
            } else {
                it.printStackTrace()
                logger.error(it) { "Error ${it.message}" }
                state.transition(DeviceEvent.Terminate)
                terminate()
            }
        }
    }

    private fun initialize() {
        logger.debug { "initialize" }
        job = async(context, parent = deviceJob) {
            device.prepare(configuration)
        }
    }

    private fun executeBatch(batch: TestBatch, result: CompletableDeferred<TestBatchResults>) {
        logger.debug { "executeBatch" }
        job = async(context, parent = deviceJob) {
            device.execute(configuration, devicePoolId, batch, result, progressReporter)
        }
    }

    private fun returnBatch(batch: TestBatch) {
        launch(parent = deviceJob) {
            pool.send(DevicePoolMessage.FromDevice.ReturnTestBatch(device, batch))
        }
    }

    private fun terminate() {
        logger.debug { "terminate" }
        job?.cancel()
        context.close()
        deviceJob.cancel()
        close()
    }
}

