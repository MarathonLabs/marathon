package com.malinskiy.marathon.ios.idb

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred

class IOSDevice : Device {
    override val operatingSystem: OperatingSystem = OperatingSystem("asd")
    override val serialNumber: String = ""
    override val model: String = ""
    override val manufacturer: String = ""
    override val networkState: NetworkState = NetworkState.CONNECTED
    override val deviceFeatures: Collection<DeviceFeature> = emptyList()
    override val healthy: Boolean = false
    override val abi: String = ""

    override suspend fun prepare(configuration: Configuration) {
    }

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {
    }

    override fun dispose() {
    }
}
