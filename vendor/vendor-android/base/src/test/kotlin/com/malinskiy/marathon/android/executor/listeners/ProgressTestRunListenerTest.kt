package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.server.junit5.AdbClient
import com.malinskiy.adam.server.junit5.AdbServer
import com.malinskiy.adam.server.junit5.AdbTest
import com.malinskiy.adam.server.stub.AndroidDebugBridgeServer
import com.malinskiy.marathon.android.adam.TestConfigurationFactory
import com.malinskiy.marathon.android.adam.TestDeviceFactory
import com.malinskiy.marathon.android.adam.boot
import com.malinskiy.marathon.android.adam.features
import com.malinskiy.marathon.android.configuration.AggregationMode
import com.malinskiy.marathon.android.configuration.FileSyncConfiguration
import com.malinskiy.marathon.android.configuration.FileSyncEntry
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@AdbTest
class ProgressTestRunListenerTest {
    @AdbClient
    lateinit var client: AndroidDebugBridgeClient

    @AdbServer
    lateinit var server: AndroidDebugBridgeServer

    @Test
    fun testDefault() {
        val configuration = TestConfigurationFactory.create(
            fileSyncConfiguration = FileSyncConfiguration(
                mutableListOf(
                    FileSyncEntry(
                        "screenshots",
                        AggregationMode.DEVICE
                    )
                )
            )
        )
        val device = TestDeviceFactory.create(client, configuration, mock())
        val progressReporter = mock<ProgressReporter>()
        val poolId = DevicePoolId("testpool")
        val listener = ProgressTestRunListener(device, poolId, progressReporter)

        runBlocking {
            server.multipleSessions {
                serial("emulator-5554") {
                    boot()
                }
                features("emulator-5554")
            }

            device.setup()

            listener.testStarted(test0)
            listener.testEnded(test0, mapOf("metric" to "value"))
            verify(progressReporter, times(1)).testStarted(poolId, device.toDeviceInfo(), test0.toTest())
            verify(progressReporter, times(1)).testPassed(poolId, device.toDeviceInfo(), test0.toTest())
            reset(progressReporter)

            listener.testStarted(test1)
            listener.testFailed(test1, "trace")
            listener.testEnded(test1, emptyMap())
            verify(progressReporter, times(1)).testStarted(poolId, device.toDeviceInfo(), test1.toTest())
            verify(progressReporter, times(1)).testFailed(poolId, device.toDeviceInfo(), test1.toTest())
            reset(progressReporter)

            listener.testStarted(test2)
            listener.testAssumptionFailure(test2, "trace")
            verify(progressReporter, times(1)).testStarted(poolId, device.toDeviceInfo(), test2.toTest())
            verify(progressReporter, times(1)).testIgnored(poolId, test2.toTest())
            reset(progressReporter)

            listener.testStarted(test3)
            listener.testIgnored(test3)
            verify(progressReporter, times(1)).testStarted(poolId, device.toDeviceInfo(), test3.toTest())
            verify(progressReporter, times(1)).testIgnored(poolId, test3.toTest())
            reset(progressReporter)
        }
    }

    companion object {
        val test0 = TestIdentifier("com.example.Class", "method0")
        val test1 = TestIdentifier("com.example.Class", "method1")
        val test2 = TestIdentifier("com.example.Class", "method2")
        val test3 = TestIdentifier("com.example.Class", "method3")
        val test4 = TestIdentifier("com.example.Class", "method4")
    }
}
