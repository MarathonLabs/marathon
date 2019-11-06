package com.malinskiy.marathon

import com.malinskiy.marathon.execution.ComponentInfo

interface MarathonRunner {
    suspend fun start()
    suspend fun scheduleTests(componentInfo: ComponentInfo)
    suspend fun waitForCompletionAndDispose(): Boolean
}
