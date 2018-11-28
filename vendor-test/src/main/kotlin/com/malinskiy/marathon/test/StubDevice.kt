package com.malinskiy.marathon.test

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.experimental.CompletableDeferred

class StubDevice(override val operatingSystem: OperatingSystem = OperatingSystem("25"),
                 override val model: String = "test",
                 override val manufacturer: String = "test",
                 override val networkState: NetworkState = NetworkState.CONNECTED,
                 override val deviceFeatures: Collection<DeviceFeature> = listOf(),
                 override val abi: String = "test",
                 override val serialNumber: String = "serial-1",
                 override val healthy: Boolean = true) : Device {

    val logger = MarathonLogging.logger(StubDevice::class.java.simpleName)

    lateinit var executionResults: Map<Test, Array<TestStatus>>
    var executionIndexMap: MutableMap<Test, Int> = mutableMapOf()

    override fun execute(configuration: Configuration, devicePoolId: DevicePoolId, testBatch: TestBatch, deferred: CompletableDeferred<TestBatchResults>, progressReporter: ProgressReporter) {
        val results = testBatch.tests.map {
            val i = executionIndexMap.getOrDefault(it, 0)
            val result = executionResults[it]!![i]
            executionIndexMap[it] = i + 1
            TestResult(it, toDeviceInfo(), result, 0, 1, null)
        }

        deferred.complete(
                TestBatchResults(this,
                        results.filter { it.isSuccess },
                        results.filter { !it.isSuccess },
                        emptySet()
                )
        )
    }

    override fun prepare(configuration: Configuration) {
        logger.debug { "Preparing" }
    }

    override fun dispose() {
        logger.debug { "Disposing" }
    }
}