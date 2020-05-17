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
import idb.CompanionServiceGrpcKt
import idb.TargetDescription
import kotlinx.coroutines.CompletableDeferred

class IOSDevice(private val idb: IdbClient, targetDescription: TargetDescription) : Device {
    override val operatingSystem: OperatingSystem = OperatingSystem(targetDescription.osVersion)
    override val serialNumber: String = targetDescription.udid
    override val model: String = targetDescription.name
    override val manufacturer: String = "Apple"
    override val networkState: NetworkState = NetworkState.CONNECTED
    override val deviceFeatures: Collection<DeviceFeature> = emptyList()
    override val healthy: Boolean = targetDescription.state == "booted"
    override val abi: String = targetDescription.architecture

    override suspend fun prepare(configuration: Configuration) {
        val iosConfiguration = configuration.vendorConfiguration as IdbConfiguration
//        idb.install(configuration.app)
//        idb.install(configuration.runner)
//        idb.installXCTest(configuration)
    }

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {
        val iosConfiguration = configuration.vendorConfiguration as IdbConfiguration

    }

    override fun dispose() {
        idb.dispose()
    }
}
