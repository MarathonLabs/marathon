package com.malinskiy.marathon.analytics

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId

interface DeviceTracker {
    fun deviceConnected(poolId: DevicePoolId,
                        device: Device)

    fun deviceDisconnected(poolId: DevicePoolId,
                           device: Device)

}