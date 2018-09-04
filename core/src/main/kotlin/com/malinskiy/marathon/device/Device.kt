package com.malinskiy.marathon.device

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.experimental.CompletableDeferred

interface Device {
    val operatingSystem: OperatingSystem
    val serialNumber: String
    val model: String
    val manufacturer: String
    val networkState: NetworkState
    val deviceFeatures: Collection<DeviceFeature>
    val healthy: Boolean
    val abi: String

    fun execute(configuration: Configuration,
                devicePoolId: DevicePoolId,
                testBatch: TestBatch,
                deferred: CompletableDeferred<TestBatchResults>,
                progressReporter: ProgressReporter)

    fun prepare(configuration: Configuration)
}

