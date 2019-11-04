package com.malinskiy.marathon.analytics.internal.pub

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class Track : Tracker {
    private val delegates: AtomicReference<MutableList<Tracker>> = AtomicReference(mutableListOf())

    operator fun plus(tracker: Tracker): Track {
        delegates.getAndUpdate { list ->
            list.add(tracker)
            list
        }
        return this
    }

    override fun deviceProviderInit(serialNumber: String, startTime: Instant, finishTime: Instant) {
        delegates.get().forEach { it.deviceProviderInit(serialNumber, startTime, finishTime) }
    }

    override fun devicePreparing(serialNumber: String, startTime: Instant, finishTime: Instant) {
        delegates.get().forEach { it.devicePreparing(serialNumber, startTime, finishTime) }
    }

    override fun deviceConnected(poolId: DevicePoolId, device: DeviceInfo) {
        delegates.get().forEach { it.deviceConnected(poolId, device) }
    }
    override fun test(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult, final: Boolean) {
        delegates.get().forEach { it.test(poolId, device, testResult, final) }
    }

    override fun close() {
        delegates.get().forEach { it.close() }
    }

    suspend fun trackDevicePreparing(device: Device, block: suspend () -> Unit) {
        val start = Instant.now()
        block.invoke()
        val finish = Instant.now()

        devicePreparing(device.serialNumber, start, finish)
    }

    suspend fun trackProviderDevicePreparing(device: Device, block: suspend () -> Unit) {
        val start = Instant.now()
        block.invoke()
        val finish = Instant.now()

        deviceProviderInit(device.serialNumber, start, finish)
    }
}
