package com.malinskiy.marathon.device

import com.malinskiy.marathon.config.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred
import mu.KLogger

interface Device {
    val operatingSystem: OperatingSystem
    val serialNumber: String
    val model: String
    val manufacturer: String
    val networkState: NetworkState
    val deviceFeatures: Collection<DeviceFeature>
    val healthy: Boolean
    val abi: String
    val logger: KLogger

    /**
     * Called before once after has been device connected
     *
     * Should throw an instance of DeviceSetupException
     *
     * @see com.malinskiy.marathon.exceptions.DeviceSetupException
     *
     */
    suspend fun prepare(configuration: Configuration)

    /**
     * Test batch execution
     *
     * This can and should throw an instance of
     * - DeviceLostException in case of unrecoverable errors that will never allow the device to execute tests
     * - TestBatchExecutionException in case the device might still be able to execute tests later
     *
     * If any other exception is thrown - it is assumed to be recoverable and will retry using this device again
     *
     * @see com.malinskiy.marathon.exceptions.DeviceLostException
     * @see com.malinskiy.marathon.exceptions.TestBatchExecutionException
     */
    suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    )

    /**
     * Called after the device has been disconnected
     */
    fun dispose()
}

