package com.malinskiy.marathon.analytics.tracker.device

import com.malinskiy.marathon.device.Device
import java.util.concurrent.ConcurrentLinkedQueue

object InMemoryDeviceTracker : DeviceTracker {
    val metrics: ConcurrentLinkedQueue<DeviceInitMetric> = ConcurrentLinkedQueue()

    override suspend fun trackDevicePreparing(device: Device, block: suspend () -> Unit) {
        val start = System.currentTimeMillis()
        block.invoke()
        val finish = System.currentTimeMillis()

        metrics.add(DeviceInitMetric(device.serialNumber, start, finish, DeviceInitType.DEVICE_PREPARE))
    }

    override suspend fun trackProviderDevicePreparing(device: Device, block: suspend () -> Unit) {
        val start = System.currentTimeMillis()
        block.invoke()
        val finish = System.currentTimeMillis()

        metrics.add(DeviceInitMetric(device.serialNumber, start, finish, DeviceInitType.DEVICE_PROVIDER_INIT))
    }
}