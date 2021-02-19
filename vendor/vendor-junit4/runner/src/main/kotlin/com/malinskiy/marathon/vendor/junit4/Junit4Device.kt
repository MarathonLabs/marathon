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
import com.malinskiy.marathon.vendor.junit4.executor.listener.TestRunResultsListener
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
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

    override suspend fun prepare(configuration: Configuration) {
        //TODO: create a long-running JVM service with gRPC interface for running tests instead of spawning JVM per execution
    }

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {
        val conf = configuration.vendorConfiguration as Junit4Configuration
        val booterFile = "booter-all.jar"
        val classpath = "${conf.applicationJar}:${conf.testsJar}:${booterFile}"
        val controlPort = 50051
        //TODO: allow specifying java executable
        val processBuilder = ProcessBuilder("java", "-cp", "$classpath", "com.malinskiy.marathon.vendor.junit4.booter.BooterKt")
        processBuilder.environment()["PORT"] = controlPort.toString()
        val process = processBuilder.start()

        val tests = testBatch.tests.map { it.toTestName() }
        val listener = CompositeTestRunListener(
            listOf(
                DebugTestRunListener(this),
                TestRunResultsListener(testBatch, this, deferred, timer, emptyList())
            )
        )

        val localChannel = ManagedChannelBuilder.forAddress("localhost", controlPort).apply {
            usePlaintext()
            executor(Dispatchers.IO.asExecutor())
        }.build()

        val client = TestExecutorClient(localChannel)
        client.execute(tests, listener)

        process.waitFor()
    }

    override fun dispose() {}
}
