package com.malinskiy.marathon.report

import com.google.gson.Gson
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.io.FileManager

class SummaryCompiler(private val configuration: Configuration,
                      private val fileManager: FileManager,
                      private val gson: Gson) {

    fun compile(pools: List<DevicePoolId>): Summary {
        TODO()
    }
}