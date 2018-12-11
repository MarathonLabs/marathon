package com.malinskiy.marathon.device

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred

class DeviceStub(override var operatingSystem: OperatingSystem = OperatingSystem("25"),
                 override var serialNumber: String = "serialNumber",
                 override var networkState: NetworkState = NetworkState.CONNECTED,
                 override var healthy: Boolean = true,
                 override val abi: String = "x64",
                 override val model: String = "model",
                 override val manufacturer: String = "manufacturer",
                 override val deviceFeatures: Collection<DeviceFeature> = emptyList()) : Device {
    override suspend fun execute(configuration: Configuration, devicePoolId: DevicePoolId, testBatch: TestBatch, deferred: CompletableDeferred<TestBatchResults>, progressReporter: ProgressReporter) {}

    override suspend fun prepare(configuration: Configuration) {}

    override fun dispose() {}
}
