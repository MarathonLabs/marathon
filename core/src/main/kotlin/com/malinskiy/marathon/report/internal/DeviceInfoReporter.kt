package com.malinskiy.marathon.report.internal

import com.google.gson.Gson
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FileType
import java.io.FileReader

class DeviceInfoReporter(private val fileManager: FileManager,
                         private val gson: Gson) {
    fun saveDeviceInfo(poolId: DevicePoolId, device: DeviceInfo) {
        val json = gson.toJson(device)
        fileManager.createFile(FileType.DEVICE_INFO, poolId, device).writeText(json)
    }

    fun getDevices(poolId: DevicePoolId): List<DeviceInfo> {
        return fileManager.getFiles(FileType.DEVICE_INFO, poolId).map {
            gson.fromJson(FileReader(it), DeviceInfo::class.java)
        }
    }
}

