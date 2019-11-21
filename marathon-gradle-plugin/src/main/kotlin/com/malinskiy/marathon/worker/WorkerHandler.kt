package com.malinskiy.marathon.worker

import com.malinskiy.marathon.execution.ComponentInfo
import com.malinskiy.marathon.execution.Configuration

interface WorkerHandler {
    fun initialize(configuration: Configuration)
    fun ensureStarted()
    fun scheduleTests(componentInfo: ComponentInfo)
    fun stop()
}
