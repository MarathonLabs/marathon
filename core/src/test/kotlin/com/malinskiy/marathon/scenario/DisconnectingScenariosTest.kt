package com.malinskiy.marathon.scenario

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestStatus
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

class DisconnectingScenariosTest {

    @AfterEach
    fun `stop koin`() {
        stopKoin()
    }

    @Test
    fun `two healthy devices on execution of two tests while one device disconnects should pass`() {
        var output: File? = null
        val context = TestCoroutineContext("testing context")
        val timerStub: Timer = mock()

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val test2 = MarathonTest("test", "SimpleTest", "test2", emptySet())
            val device1 = StubDevice(serialNumber = "serial-1")
            val device2 = StubDevice(serialNumber = "serial-2")

            configuration {
                output = outputDir
                timer = timerStub

                tests {
                    listOf(test1, test2)
                }

                deviceProvider.context = context

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                    delay(100)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device2))
                    delay(5000)
                    it.send(DeviceProvider.DeviceEvent.DeviceDisconnected(device1))
                }
            }

            device1.executionResults = mapOf(
                test1 to arrayOf(TestStatus.INCOMPLETE),
                test2 to arrayOf(TestStatus.INCOMPLETE)
            )
            device2.executionResults = mapOf(
                test1 to arrayOf(TestStatus.PASSED),
                test2 to arrayOf(TestStatus.PASSED)
            )
        }

        var i = 0L
        whenever(timerStub.currentTimeMillis()).then { i++ }

        val job = GlobalScope.launch(context = context) {
            marathon.runAsync()
        }

        context.advanceTimeBy(20, TimeUnit.SECONDS)

        job.isCompleted shouldBe true

        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/disconnecting_scenario_1.json").file))
    }
}
