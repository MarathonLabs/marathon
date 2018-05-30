package com.malinskiy.marathon.analytics

import com.google.gson.Gson
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType

class LocalDeviceTracker(private val fileManager: FileManager,
                         private val gson: Gson) : DeviceTracker {
    override fun deviceConnected(poolId: DevicePoolId, device: Device) {
        val json = gson.toJson(device)
        fileManager.createFile(FileType.DEVICE_INFO, poolId, device).writeText(json)
    }

    override fun deviceDisconnected(poolId: DevicePoolId, device: Device) {
        
    }
}