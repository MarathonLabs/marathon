package com.malinskiy.marathon.vendor.junit4

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.junit4.client.TestExecutorClient
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import com.malinskiy.marathon.vendor.junit4.executor.listener.CompositeTestRunListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.DebugTestRunListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.ProgressTestRunListener
import com.malinskiy.marathon.vendor.junit4.executor.listener.TestRunResultsListener
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.File
import java.util.*

class Junit4Device(protected val timer: Timer) : Device {
    override val operatingSystem: OperatingSystem = OperatingSystem(System.getProperty("os.name") ?: "")
    override val serialNumber: String = UUID.randomUUID().toString()
    override val model: String = System.getProperty("java.version")
    override val manufacturer: String = System.getProperty("java.vendor")
    override val networkState: NetworkState = NetworkState.CONNECTED
    override val deviceFeatures: Collection<DeviceFeature> = emptySet()
    override val healthy: Boolean = true
    override val abi: String = System.getProperty("os.arch")

    private lateinit var client: TestExecutorClient
    private lateinit var process: Process

    override suspend fun prepare(configuration: Configuration) {
        val conf = configuration.vendorConfiguration as Junit4Configuration
        val booterFile = javaClass.getResource("/booter-all.jar").file
        val applicationJar = File(conf.applicationJar.toURI())
        val testJar = File(conf.testsJar.toURI())
        val classpath = "$applicationJar:$testJar:$booterFile"
        val controlPort = 50051
        //TODO: allow specifying java executable
        process = ProcessBuilder("java", "-cp", "$classpath", "com.malinskiy.marathon.vendor.junit4.booter.BooterKt")
            .apply {
                environment()["PORT"] = controlPort.toString()
                inheritIO()
            }.start()

        val localChannel = ManagedChannelBuilder.forAddress("localhost", controlPort).apply {
            usePlaintext()
            executor(Dispatchers.IO.asExecutor())
        }.build()

        client = TestExecutorClient(localChannel)
    }

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {
        val tests = testBatch.tests.map { it.toTestName() }
        val listener = CompositeTestRunListener(
            listOf(
                DebugTestRunListener(this),
                ProgressTestRunListener(this, devicePoolId, progressReporter),
                TestRunResultsListener(testBatch, this, deferred, timer, emptyList())
            )
        )

        client.execute(tests, listener)
    }

    override fun dispose() {
        client.close()
        process.destroy()
        process.waitFor()
    }
}
