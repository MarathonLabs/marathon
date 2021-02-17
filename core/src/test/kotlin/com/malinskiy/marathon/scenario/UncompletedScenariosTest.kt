package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.FixedQuotaRetryStrategy
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.assert.shouldBeEqualToAsJson
import com.malinskiy.marathon.test.setupMarathon
import com.malinskiy.marathon.time.Timer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineContext
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import java.io.File
import java.util.concurrent.TimeUnit
import com.malinskiy.marathon.test.Test as MarathonTest

class UncompletedScenariosTest {

    @AfterEach
    fun `stop koin`() {
        stopKoin()
    }

    @Test
    fun `one device that never completes tests with 100 uncompleted tests executed should return`() {
        var output: File? = null
        val context = TestCoroutineContext("testing context")

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val device1 = StubDevice(serialNumber = "serial-1")

            configuration {
                output = outputDir

                tests {
                    listOf(test1)
                }

                uncompletedTestRetryQuota = 100

                vendorConfiguration.deviceProvider.context = context

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                }
            }

            device1.executionResults = mapOf(test1 to Array(101) { TestStatus.INCOMPLETE })
        }

        val job = GlobalScope.launch(context = context) {
            marathon.runAsync()
        }

        context.advanceTimeBy(600, TimeUnit.SECONDS)

        job.isCompleted shouldBe true

        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/uncompleted_scenario_1.json").file))
    }

    @Test
    fun `one device that never completes tests with 100 uncompleted tests while throwing exception should return`() {
        var output: File? = null
        val context = TestCoroutineContext("testing context")
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

                vendorConfiguration.deviceProvider.context = context

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                }
            }

            device1.executionResults = mapOf(test1 to Array(101) { TestStatus.INCOMPLETE })
        }

        var i = 0L
        whenever(timerMock.currentTimeMillis()).then { i++ }

        val job = GlobalScope.launch(context = context) {
            marathon.runAsync()
        }

        context.advanceTimeBy(600, TimeUnit.SECONDS)

        job.isCompleted shouldBe true

        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/uncompleted_scenario_1.json").file))
    }

    @Test
    fun `one device that never completes tests after all retries should report test as failed`() {
        var output: File? = null
        val context = TestCoroutineContext("testing context")

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val device1 = StubDevice(serialNumber = "serial-1")

            configuration {
                output = outputDir

                tests {
                    listOf(test1)
                }

                uncompletedTestRetryQuota = 3

                vendorConfiguration.deviceProvider.context = context

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                }
            }

            device1.executionResults = mapOf(test1 to Array(4) { TestStatus.INCOMPLETE })
        }

        val job = GlobalScope.launch(context = context) {
            marathon.runAsync()
        }

        context.advanceTimeBy(600, TimeUnit.SECONDS)

        job.isCompleted shouldBe true

        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/uncompleted_scenario_2.json").file))
    }

    @Test
    fun `one device that never completes tests after all retries with retry strategy should report test as failed`() {
        var output: File? = null
        val context = TestCoroutineContext("testing context")

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val device1 = StubDevice(serialNumber = "serial-1")

            configuration {
                output = outputDir

                tests {
                    listOf(test1)
                }

                uncompletedTestRetryQuota = 3
                retryStrategy = FixedQuotaRetryStrategy(10, 3)

                vendorConfiguration.deviceProvider.context = context

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                }
            }

            device1.executionResults = mapOf(test1 to Array(100) { TestStatus.INCOMPLETE })
        }

        val job = GlobalScope.launch(context = context) {
            marathon.runAsync()
        }

        context.advanceTimeBy(600, TimeUnit.SECONDS)

        job.isCompleted shouldBe true

        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/uncompleted_scenario_2.json").file))
    }
}
