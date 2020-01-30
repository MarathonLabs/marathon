package com.malinskiy.marathon.analytics.internal.pub

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList

class Track : Tracker {
    private val delegates: CopyOnWriteArrayList<Tracker> = CopyOnWriteArrayList()

    fun add(tracker: Tracker): Track {
        delegates.add(tracker)
        return this
    }

    override fun deviceProviderInit(serialNumber: String, startTime: Instant, finishTime: Instant) {
        delegates.forEach { it.deviceProviderInit(serialNumber, startTime, finishTime) }
    }

    override fun devicePreparing(serialNumber: String, startTime: Instant, finishTime: Instant) {
        delegates.forEach { it.devicePreparing(serialNumber, startTime, finishTime) }
    }

    override fun deviceConnected(poolId: DevicePoolId, device: DeviceInfo) {
        delegates.forEach { it.deviceConnected(poolId, device) }
    }

    override fun test(poolId: DevicePoolId, device: DeviceInfo, testResult: TestResult, final: Boolean) {
        delegates.forEach { it.test(poolId, device, testResult, final) }
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
