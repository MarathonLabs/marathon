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
import mu.KLogger
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates.observable

class DeviceActor(private val devicePoolId: DevicePoolId,
                  private val pool: SendChannel<DevicePoolMessage>,
                  private val configuration: Configuration,
                  val device: Device,
                  private val progressReporter: ProgressReporter,
                  parent: Job,
                  context: CoroutineContext) :
        Actor<DeviceEvent>(parent = parent, context = context) {

    private val logger = MarathonLogging.logger("DevicePool[${devicePoolId.name}]_DeviceActor[${device.serialNumber}]")
    private val karma = DeviceKarmaCounter(logger)

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
                transitionTo(DeviceState.Running(it.batch), DeviceAction.ExecuteBatch(it.batch))
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
                transitionTo(DeviceState.Terminated, DeviceAction.StopAndTerminatee)
            }
            on<DeviceEvent.RunningComplete> {
                val result = device.getResults()
                karma.processResults(result)

                when (karma.tellMyFate()) {
                    DeviceKarmaCounter.Fate.Live ->
                        transitionTo(DeviceState.Ready, DeviceAction.SendResultAndNotifyIsReady)

                    DeviceKarmaCounter.Fate.Initialize ->
                        transitionTo(DeviceState.Initializing, DeviceAction.ReturnBatchAndInitialize)

                    DeviceKarmaCounter.Fate.Die ->
                        transitionTo(DeviceState.Terminated, DeviceAction.StopAndTerminatee)
                }
            }
            on<DeviceEvent.WakeUp> {
                dontTransition()
            }
            on<DeviceEvent.Initialize> {
                val result = device.getResults()
                karma.processResults(result)

                transitionTo(DeviceState.Initializing, DeviceAction.ReturnBatchAndInitialize)
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
                    sendResults()
                    notifyIsReady()
                }
                is DeviceAction.NotifyIsReady -> {
                    notifyIsReady()
                }
                is DeviceAction.ExecuteBatch -> {
                    executeBatch(sideEffect.batch)
                }
                is DeviceAction.Terminate -> {
                    terminate()
                }
                is DeviceAction.StopAndTerminatee -> {
                    sendResults()
                    terminate()
                }
                is DeviceAction.ReturnBatchAndInitialize -> {
                    sendResults()
                    initialize()
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

    private fun sendResults() {
        launch {
            val result = device.getResults()
            pool.send(DevicePoolMessage.FromDevice.CompletedTestBatch(device, result))
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
                withRetry(2, 10000) {
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

    private fun executeBatch(batch: TestBatch) {
        logger.debug { "executeBatch ${device.serialNumber}" }
        job = async {
            try {
                logger.debug("batch started")
                device.execute(configuration, devicePoolId, batch, progressReporter)
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

class DeviceKarmaCounter(val logger: KLogger) {
    var failedTestsInRaw = 0
    var failedBatchesInRaw = 0

    fun processResults(testResults: TestBatchResults) {
        if (testResults.passed.size > 0) {
            failedBatchesInRaw = 0
            failedTestsInRaw = 0
        } else {
            if (testResults.passed.size + testResults.failed.size + testResults.missed.size + testResults.incomplete.size > 3) {
                failedBatchesInRaw++
            }

            failedTestsInRaw += testResults.failed.size + testResults.incomplete.size
        }

        logger.debug { "karma now is $failedTestsInRaw failed tests + $failedBatchesInRaw failed batches" }
    }

    fun tellMyFate(): Fate {
        val fate = when {
            failedBatchesInRaw >= 2 -> Fate.Die
            failedBatchesInRaw >= 1 -> Fate.Initialize
            failedTestsInRaw >= 8 -> Fate.Die
            else -> Fate.Live
        }

        logger.debug { "karma decision is to ${fate::class.simpleName}" }

        return fate
    }

    sealed class Fate {
        object Live : Fate()
        object Initialize : Fate()
        object Die : Fate()
    }
}

