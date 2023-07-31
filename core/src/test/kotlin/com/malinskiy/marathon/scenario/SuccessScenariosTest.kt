@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.test.StubDevice
import com.malinskiy.marathon.test.assert.shouldBeEqualToAsJson
import com.malinskiy.marathon.test.setupMarathon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import com.malinskiy.marathon.test.Test as MarathonTest

class SuccessScenariosTest {
    @AfterEach
    fun `stop koin`() {
        stopKoin()
    }

    @Test
    fun `one healthy device execution of one test should pass`() = runTest {
        var output: File? = null

        val marathon = setupMarathon {
            val test = MarathonTest("test", "SimpleTest", "test", emptySet())
            val device = StubDevice()

            configuration {
                output = outputDir

                tests {
                    listOf(test)
                }

                deviceProvider.context = coroutineContext

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device))
                }
            }

            device.executionResults = mapOf(
                test to arrayOf(TestStatus.PASSED)
            )
        }

        val job = launch {
            marathon.runAsync()
        }

        advanceTimeBy(20_000)

        job.isCompleted shouldBe true
        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/success_scenario_1.json").file))
    }
}
