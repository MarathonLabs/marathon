package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.assert.shouldBeEqualToAsJson
import com.malinskiy.marathon.test.setupMarathon
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
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
        val context = TestCoroutineScope("testing context")

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
        val context = TestCoroutineScope("testing context")

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val device1 = StubDevice(serialNumber = "serial-1", crashWithTestBatchException = true)

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

        /**
         * Since there are no guarantees about vendor modules reporting failed tests we need to restart the whole batch
         * but then there is a chance that after all the retry quota is exhausted it still didn't finish
         * To mitigate this please report your uncompleted tests
         */
        File(output!!.absolutePath + "/test_result", "raw.json").readText().shouldBeEqualTo("[]")
    }
}
