@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.setupMarathon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import kotlin.time.ExperimentalTime
import com.malinskiy.marathon.test.Test as MarathonTest

class TwoPoolsDisconnectScenariosTest {

    @AfterEach
    fun `stop koin`() {
        stopKoin()
    }

    @Test
    fun `two devices in separate pools disconnecting one after another should terminate the run`() = runTest {
        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val test2 = MarathonTest("test", "SimpleTest", "test2", emptySet())
            // modelA is iterated before modelZ in Scheduler.pools, so the RemoveDevice broadcast reaches
            // the already terminated pool before the live one - the ordering that triggers the deadlock
            val deviceA = StubDevice(serialNumber = "serial-1", model = "modelA")
            // deviceB never finishes on its own, so only a delivered RemoveDevice can terminate its pool
            val deviceB = StubDevice(serialNumber = "serial-2", model = "modelZ", prepareTimeMillis = 600_000L)

            configuration {
                poolingStrategy = PoolingStrategyConfiguration.ModelPoolingStrategyConfiguration

                tests {
                    listOf(test1, test2)
                }

                deviceProvider.context = coroutineContext

                devices {
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(deviceA))
                    delay(100)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(deviceB))
                    delay(2000)
                    it.send(DeviceProvider.DeviceEvent.DeviceDisconnected(deviceA))
                    delay(2000)
                    it.send(DeviceProvider.DeviceEvent.DeviceDisconnected(deviceB))
                }
            }

            deviceA.executionResults = mapOf(
                test1 to arrayOf(TestStatus.PASSED),
                test2 to arrayOf(TestStatus.PASSED)
            )
            deviceB.executionResults = mapOf(
                test1 to arrayOf(TestStatus.PASSED),
                test2 to arrayOf(TestStatus.PASSED)
            )
        }

        val job = launch {
            marathon.runAsync()
        }

        advanceTimeBy(30_000)

        job.isCompleted shouldBe true
    }
}
