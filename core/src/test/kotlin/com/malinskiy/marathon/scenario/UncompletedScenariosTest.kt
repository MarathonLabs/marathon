@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.config.strategy.RetryStrategyConfiguration
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.assert.shouldBeEqualToAsJson
import com.malinskiy.marathon.test.setupMarathon
import com.malinskiy.marathon.time.Timer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import java.io.File
import kotlin.time.ExperimentalTime
import com.malinskiy.marathon.test.Test as MarathonTest

class UncompletedScenariosTest {

    @AfterEach
    fun `stop koin`() {
        stopKoin()
    }

    @Test
    fun `one device that never completes tests with 100 uncompleted tests executed should return`() = runTest {
        var output: File? = null

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val device1 = StubDevice(serialNumber = "serial-1")

            configuration {
                output = outputDir

                tests {
                    listOf(test1)
                }

                uncompletedTestRetryQuota = 100

                deviceProvider.context = coroutineContext

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                }
            }

            device1.executionResults = mapOf(test1 to Array(101) { TestStatus.INCOMPLETE })
        }

        val job = launch {
            marathon.runAsync()
        }

        advanceTimeBy(600_000)

        job.isCompleted shouldBe true

        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/uncompleted_scenario_1.json").file))
    }

    @Test
    fun `one device that never completes tests with 100 uncompleted tests while throwing exception should return`() = runTest {
        var output: File? = null
        val timerMock: Timer = mock()

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val device1 = StubDevice(serialNumber = "serial-1", crashWithTestBatchException = true)

            configuration {
                output = outputDir
                timer = timerMock

                tests {
                    listOf(test1)
                }

                uncompletedTestRetryQuota = 100

                deviceProvider.context = coroutineContext

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                }
            }

            device1.executionResults = mapOf(test1 to Array(101) { TestStatus.INCOMPLETE })
        }

        var i = 0L
        whenever(timerMock.currentTimeMillis()).then { i++ }

        val job = launch {
            marathon.runAsync()
        }

        advanceTimeBy(600_000)

        job.isCompleted shouldBe true

        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/uncompleted_scenario_1.json").file))
    }

    @Test
    fun `one device that never completes tests after all retries should report test as failed`() = runTest {
        var output: File? = null

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val device1 = StubDevice(serialNumber = "serial-1")

            configuration {
                output = outputDir

                tests {
                    listOf(test1)
                }

                uncompletedTestRetryQuota = 3

                deviceProvider.context = coroutineContext

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                }
            }

            device1.executionResults = mapOf(test1 to Array(4) { TestStatus.INCOMPLETE })
        }

        val job = launch {
            marathon.runAsync()
        }

        advanceTimeBy(600_000)

        job.isCompleted shouldBe true

        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/uncompleted_scenario_2.json").file))
    }

    @Test
    fun `one device that never completes tests after all retries with retry strategy should report test as failed`() = runTest {
        var output: File? = null

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val device1 = StubDevice(serialNumber = "serial-1")

            configuration {
                output = outputDir

                tests {
                    listOf(test1)
                }

                uncompletedTestRetryQuota = 3
                retryStrategy = RetryStrategyConfiguration.FixedQuotaRetryStrategyConfiguration(10, 3)

                deviceProvider.context = coroutineContext

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                }
            }

            device1.executionResults = mapOf(test1 to Array(100) { TestStatus.INCOMPLETE })
        }

        val job = launch {
            marathon.runAsync()
        }

        advanceTimeBy(600_000)

        job.isCompleted shouldBe true

        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/uncompleted_scenario_2.json").file))
    }
}
