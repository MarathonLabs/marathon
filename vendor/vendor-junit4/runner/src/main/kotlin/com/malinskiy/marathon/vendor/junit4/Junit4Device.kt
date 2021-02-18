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
import com.malinskiy.marathon.vendor.junit4.configuration.Junit4Configuration
import kotlinx.coroutines.CompletableDeferred
import java.util.*

class Junit4Device : Device {
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
        //TODO: allow specifying java executable
        val tests = testBatch.tests.joinToString(",") { "${it.toTestName()}" }
        val processBuilder = ProcessBuilder("java", "-cp", "$classpath", "com.malinskiy.marathon.vendor.junit4.booter.BooterKt", "$tests")
        processBuilder.inheritIO()
        val process = processBuilder.start()
        process.waitFor()
    }

    override fun dispose() {}
}
