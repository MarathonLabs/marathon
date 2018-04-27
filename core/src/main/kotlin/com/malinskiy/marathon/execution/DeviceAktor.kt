package com.malinskiy.marathon.execution

import com.malinskiy.marathon.aktor.Aktor
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext

class DeviceAktor(
    private val pool: Aktor<DevicePoolMessage>,
    private val configuration: Configuration,
    private val device: Device
) : Aktor<DeviceMessage>() {

    override suspend fun receive(msg: DeviceMessage) {
        when (msg) {
            is DeviceMessage.Initialize -> initialize()
            is DeviceMessage.ExecuteTestBatch -> executeBatch(msg.batch)
            is DeviceMessage.Terminate -> terminate()
        }
    }

    private suspend fun initialize() {
        launch(context) {
            device.prepare(configuration)
        }.join()
        pool.send(DevicePoolMessage.Ready(device, this@DeviceAktor))
    }

    private var job: Job? = null

    private val context = newFixedThreadPoolContext(1, "Device serial = ${device.serialNumber}")

    private fun executeBatch(batch: TestBatch) {
        job = launch(context) {
            device.execute(configuration, batch)
            pool.send(DevicePoolMessage.TestExecutionFinished(device, this@DeviceAktor))
        }
    }

    private fun terminate() {
        job?.cancel()
        context.close()
        close()
    }
}

sealed class DeviceMessage {
    data class ExecuteTestBatch(val batch: TestBatch) : DeviceMessage()
    object Initialize : DeviceMessage()
    object Terminate : DeviceMessage()
}
