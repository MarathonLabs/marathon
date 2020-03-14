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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import java.io.File
import java.util.concurrent.TimeUnit
import com.malinskiy.marathon.test.Test as MarathonTest

class DeviceFilteringScenarioTest {

    @AfterEach
    fun `stop koin`() {
        stopKoin()
    }

    fun `one blacklisted device and empty whitelist executing two tests should pass on one device`() {
        var output: File? = null
        val context = TestCoroutineScope("testing context")

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val test2 = MarathonTest("test", "SimpleTest", "test2", emptySet())
            val device1 = StubDevice(serialNumber = "emulator-5000")
            val device2 = StubDevice(serialNumber = "emulator-5002")

            configuration {
                output = outputDir

                tests {
                    listOf(test1, test2)
                }

                excludeSerialRegexes = listOf("""emulator-5002""".toRegex())
                includeSerialRegexes = emptyList()

                vendorConfiguration.deviceProvider.context = context

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device2))
                }
            }

            device1.executionResults = mapOf(
                test1 to arrayOf(TestStatus.PASSED),
                test2 to arrayOf(TestStatus.PASSED)
            )
        }

        val job = GlobalScope.launch(context = context) {
            marathon.runAsync()
        }

        context.advanceTimeBy(20, TimeUnit.SECONDS)

        job.isCompleted shouldBe true
        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/device_filtering_1.json").file))
    }


    @Test
    fun `one whitelisted device and empty blacklist executing two tests should pass on one device`() {
        var output: File? = null
        val context = TestCoroutineScope("testing context")

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val test2 = MarathonTest("test", "SimpleTest", "test2", emptySet())
            val device1 = StubDevice(serialNumber = "emulator-5000")
            val device2 = StubDevice(serialNumber = "emulator-5002")

            configuration {
                output = outputDir

                tests {
                    listOf(test1, test2)
                }

                excludeSerialRegexes = emptyList()
                includeSerialRegexes = listOf("""emulator-5002""".toRegex())

                vendorConfiguration.deviceProvider.context = context

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device2))
                }
            }

            device2.executionResults = mapOf(
                test1 to arrayOf(TestStatus.PASSED),
                test2 to arrayOf(TestStatus.PASSED)
            )
        }

        val job = GlobalScope.launch(context = context) {
            marathon.runAsync()
        }

        context.advanceTimeBy(20, TimeUnit.SECONDS)

        job.isCompleted shouldBe true
        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/device_filtering_2.json").file))
    }

    @Test
    fun `one blacklisted device and one whitelisted executing two tests should pass on one device`() {
        var output: File? = null
        val context = TestCoroutineScope("testing context")

        val marathon = setupMarathon {
            val test1 = MarathonTest("test", "SimpleTest", "test1", emptySet())
            val test2 = MarathonTest("test", "SimpleTest", "test2", emptySet())
            val device1 = StubDevice(serialNumber = "emulator-5000")
            val device2 = StubDevice(serialNumber = "emulator-5002")
            val device3 = StubDevice(serialNumber = "emulator-5004")

            configuration {
                output = outputDir

                tests {
                    listOf(test1, test2)
                }

                excludeSerialRegexes = listOf("""emulator-5002""".toRegex())
                includeSerialRegexes = listOf("""emulator-500[2,4]""".toRegex())

                vendorConfiguration.deviceProvider.context = context

                devices {
                    delay(1000)
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device1))
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device2))
                    it.send(DeviceProvider.DeviceEvent.DeviceConnected(device3))
                }
            }

            device3.executionResults = mapOf(
                test1 to arrayOf(TestStatus.PASSED),
                test2 to arrayOf(TestStatus.PASSED)
            )
        }

        val job = GlobalScope.launch(context = context) {
            marathon.runAsync()
        }

        context.advanceTimeBy(20, TimeUnit.SECONDS)

        job.isCompleted shouldBe true
        File(output!!.absolutePath + "/test_result", "raw.json")
            .shouldBeEqualToAsJson(File(javaClass.getResource("/output/raw/device_filtering_3.json").file))
    }
}
