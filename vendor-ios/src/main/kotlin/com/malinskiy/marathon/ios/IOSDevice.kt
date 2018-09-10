package com.malinskiy.marathon.ios


import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch

import kotlinx.coroutines.experimental.CompletableDeferred
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class IOSDevice : Device {
    override val operatingSystem: OperatingSystem
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val serialNumber: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val model: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val manufacturer: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val networkState: NetworkState
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val deviceFeatures: Collection<DeviceFeature>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val healthy: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val abi: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun execute(configuration: Configuration,
                         devicePoolId: DevicePoolId,
                         testBatch: TestBatch,
                         deferred: CompletableDeferred<TestBatchResults>,
                         progressReporter: ProgressReporter) {
    }

    override fun prepare(configuration: Configuration) {
    }
}
